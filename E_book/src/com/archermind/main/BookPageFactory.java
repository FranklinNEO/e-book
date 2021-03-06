/**
 * @Project:Archermindreader
 * @ClassName:BookPageFactory
 * @Version 1.0
 * @Author shaojian.ni xue.xia xuegang.fu
 * @Update xuegang.fu
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.Log;

public class BookPageFactory {

	private int mMbBufLen = 0;
	private int mMbBufBegin = 0;
	private int mMbBufEnd = 0;
	private int mWidth;
	private int mHeight;
	private int mFontSize = 24;
	private int mTextColor = Color.BLACK;
	private int mBackColor = 0xffff9e85; // background color
	private int mMarginWidth = 15; // the distance between the edge and left or
									// right
	private int mMarginHeight = 20; // the distance between the edge and top or
									// bottom
	private int mLineCount; // Each page can display the number of lines
	public static float sPercent;
	private float mVisibleHeight; // page height
	private float mVisibleWidth; // page width
	private boolean mIsfirstPage, mIslastPage;
	private Vector<String> mLines = new Vector<String>();
	private String mStrCharsetName = "GB2312";
	private Bitmap mBookbg = null;
	private File mBookfile = null;
	private MappedByteBuffer mMbBuf = null;
	private Paint mPaint;
	private Paint mPaint1;

	/**
	 * constructor method
	 * 
	 * @param w
	 * @param h
	 */
	public BookPageFactory(int w, int h) {
		mWidth = w;
		mHeight = h;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Align.LEFT);
		mPaint.setTextSize(mFontSize);
		mPaint.setColor(mTextColor);
		mPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint1.setTextAlign(Align.LEFT);
		mPaint1.setTextSize(18);
		mPaint1.setColor(mTextColor);
		mVisibleWidth = mWidth - mMarginWidth * 2;
		mVisibleHeight = mHeight - mMarginHeight * 2;
		// The number of rows to display
		mLineCount = (int) (mVisibleHeight / mFontSize);
	}

	/**
	 * Read local book
	 * 
	 * @param filePath
	 *            File Path
	 * @throws IOException
	 */
	public void openbook(String strFilePath) throws IOException {

		mBookfile = new File(strFilePath);
		long lLen = mBookfile.length();
		mMbBufLen = (int) lLen;
		mMbBuf = new RandomAccessFile(mBookfile, "r").getChannel().map(
				FileChannel.MapMode.READ_ONLY, 0, lLen);
	}

	/**
	 * Read paragraph back.According to the coding method
	 * 
	 * @param nFromPos
	 * @return
	 */
	protected byte[] readParagraphBack(int nFromPos) {
		int nEnd = nFromPos;
		int i;
		byte b0, b1;
		if (mStrCharsetName.equals("UTF-16LE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = mMbBuf.get(i);
				b1 = mMbBuf.get(i + 1);
				if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}
		} else if (mStrCharsetName.equals("UTF-16BE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = mMbBuf.get(i);
				b1 = mMbBuf.get(i + 1);
				if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}
		} else {
			i = nEnd - 1;
			while (i > 0) {
				b0 = mMbBuf.get(i);
				if (b0 == 0x0a && i != nEnd - 1) {
					i++;
					break;
				}
				i--;
			}
		}
		if (i < 0)
			i = 0;
		int nParaSize = nEnd - i;
		int j;
		byte[] buf = new byte[nParaSize];
		for (j = 0; j < nParaSize; j++) {
			buf[j] = mMbBuf.get(i + j);
		}
		return buf;
	}

	/**
	 * read Paragraph Forward
	 * 
	 * @param nFromPos
	 * @return buf
	 */
	protected byte[] readParagraphForward(int nFromPos) {
		int nStart = nFromPos;
		int i = nStart;
		byte b0, b1;
		// Wrap the discretion of the encoding format
		if (mStrCharsetName.equals("UTF-16LE")) {
			while (i < mMbBufLen - 1) {
				b0 = mMbBuf.get(i++);
				b1 = mMbBuf.get(i++);

				if (b0 == 0x0a && b1 == 0x00) {
					break;
				}
			}
		} else if (mStrCharsetName.equals("UTF-16BE")) {

			while (i < mMbBufLen - 1) {
				b0 = mMbBuf.get(i++);
				b1 = mMbBuf.get(i++);

				if (b0 == 0x00 && b1 == 0x0a) {
					break;
				}
			}
		} else {

			while (i < mMbBufLen) {
				b0 = mMbBuf.get(i++);
				if (b0 == 0x0a) {
					break;
				}
			}
		}
		int nParaSize = i - nStart;
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++) {
			buf[i] = mMbBuf.get(nFromPos + i);
		}
		return buf;
	}

	/**
	 * next page
	 */
	protected Vector<String> pageDown() {
		String strParagraph = "";
		Vector<String> lines = new Vector<String>();
		while (lines.size() < mLineCount && mMbBufEnd < mMbBufLen) {
			byte[] paraBuf = readParagraphForward(mMbBufEnd); // Read a paragraph
			mMbBufEnd += paraBuf.length;
			try {
				strParagraph = new String(paraBuf, mStrCharsetName);
				Log.d("Paragraph",strParagraph);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String strReturn = "";
			if (strParagraph.indexOf("\r\n") != -1) {
				strReturn = "\r\n";
				strParagraph = strParagraph.replaceAll("\r\n", "");
			} else if (strParagraph.indexOf("\n") != -1) {
				strReturn = "\n";
				strParagraph = strParagraph.replaceAll("\n", "");
			}

			if (strParagraph.length() == 0) {
				lines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				lines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
				if (lines.size() >= mLineCount) {
					break;
				}
			}
			if (strParagraph.length() != 0) {
				try {
					mMbBufEnd -= (strParagraph + strReturn)
							.getBytes(mStrCharsetName).length;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return lines;
	}

	/**
	 * up page
	 */
	protected void pageUp() {
		if (mMbBufBegin < 0)
			mMbBufBegin = 0;
		Vector<String> lines = new Vector<String>();
		String strParagraph = "";
		while (lines.size() < mLineCount && mMbBufBegin > 0) {
			Vector<String> paraLines = new Vector<String>();
			byte[] paraBuf = readParagraphBack(mMbBufBegin);
			mMbBufBegin -= paraBuf.length;
			try {
				strParagraph = new String(paraBuf, mStrCharsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			strParagraph = strParagraph.replaceAll("\r\n", "");
			strParagraph = strParagraph.replaceAll("\n", "");

			if (strParagraph.length() == 0) {
				paraLines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				paraLines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
			}
			lines.addAll(0, paraLines);
		}
		while (lines.size() > mLineCount) {
			try {
				mMbBufBegin += lines.get(0).getBytes(mStrCharsetName).length;
				lines.remove(0);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		mMbBufEnd = mMbBufBegin;
		return;
	}

	/**
	 * jump page by percent
	 * 
	 * @param percent
	 */
	protected void pageJump(float percent) {
		String strParagraph = "";
		Vector<String> lines = new Vector<String>();
		while (lines.size() < mLineCount && mMbBufEnd < mMbBufLen) {
			mMbBufBegin = (int) (mMbBufLen * percent / 100);
			Vector<String> paraLines = new Vector<String>();
			byte[] paraBuf = readParagraphBack(mMbBufBegin);
			mMbBufBegin -= paraBuf.length;
			try {
				strParagraph = new String(paraBuf, mStrCharsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			strParagraph = strParagraph.replaceAll("\r\n", "");
			strParagraph = strParagraph.replaceAll("\n", "");

			if (strParagraph.length() == 0) {
				paraLines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				paraLines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
			}
			lines.addAll(0, paraLines);
		}
		while (lines.size() > mLineCount) {
			try {
				mMbBufBegin += lines.get(0).getBytes(mStrCharsetName).length;
				lines.remove(0);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		mMbBufEnd = mMbBufBegin;
		return;
	}

	/**
	 * jump to a page by percent
	 * 
	 * @param percent
	 * @throws IOException
	 */
	protected void jumpPage(float percent) throws IOException {
		mIsfirstPage = false;
		mLines.clear();
		pageJump(percent);
		mLines = pageDown();
	}

	/**
	 * Previous Page
	 * 
	 * @throws IOException
	 */
	protected void prePage() throws IOException {
		if (mMbBufBegin <= 0) {
			mMbBufBegin = 0;
			mIsfirstPage = true;
			return;
		} else
			mIsfirstPage = false;
		mLines.clear();
		pageUp();
		mLines = pageDown();
	}

	/**
	 * next page
	 * 
	 * @throws IOException
	 */
	public void nextPage() throws IOException {
		if (mMbBufEnd >= mMbBufLen) {
			mIslastPage = true;
			return;
		} else
			mIslastPage = false;
		mLines.clear();
		mMbBufBegin = mMbBufEnd;
		mLines = pageDown();
	}

	/**
	 * draw page. current page,percent,date and time
	 * 
	 * @param c
	 */
	public void onDraw(Canvas c) {
		if (mLines.size() == 0) {
			mLines = pageDown();
			if (mBookbg == null) {
				c.drawColor(mBackColor);
			} else {
				c.drawBitmap(mBookbg, 0, 0, null);
				c.drawText("Text is empty", mWidth - 300, mHeight - 500, mPaint);
			}
		}
		if (mLines.size() > 0) {
			if (mBookbg == null) {
				c.drawColor(mBackColor);
			} else {
				Matrix mMatrix = ArchermindReaderActivity.sMatrix;
				c.drawBitmap(mBookbg, mMatrix, mPaint);
			}
			int y = mMarginHeight;
			for (String strLine : mLines) {
				y += mFontSize;
				c.drawText(strLine, mMarginWidth, y, mPaint);
			}
		}
		float fPercent = (float) (mMbBufEnd * 1.0 / mMbBufLen);
		sPercent = fPercent;
		DecimalFormat df = new DecimalFormat("#0.0");
		String strPercent = df.format(fPercent * 100) + "%";
		int nPercentWidth = (int) mPaint1.measureText("999.9%") + 1;
		java.text.DateFormat df1 = new java.text.SimpleDateFormat(
				"MM-dd HH:mm");
		String time = df1.format(new Date());
		c.drawText(strPercent, mWidth - nPercentWidth, mHeight - 5, mPaint1);
		c.drawText(time, 10, mHeight - 5, mPaint1);
	}

	/**
	 * set the background
	 * 
	 * @param BG
	 */
	public void setBgBitmap(Bitmap BG) {
		mBookbg = BG;
	}

	/**
	 * judge the text if it is first page
	 * 
	 * @return
	 */
	public boolean isfirstPage() {
		return mIsfirstPage;
	}

	/**
	 * judge the text if it is last page
	 * 
	 * @return
	 */
	public boolean islastPage() {
		return mIslastPage;
	}
}