/**
 * @Project:Archermindreader
 * @ClassName:ArchermindReaderActivity
 * @Version 1.0
 * @Author shaojian.ni xue.xia xuegang.fu
 * @Update xuegang.fu
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.main;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.*;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.archermind.mark.MTagAdapter;
import com.archermind.mark.ReaderDataBase;
import com.archermind.mark.ReaderTags;
import com.archermind.page.PageWidget;

public class ArchermindReaderActivity extends Activity implements
		OnTouchListener, SeekBar.OnSeekBarChangeListener {

	public static Matrix sMatrix;
	public static int sPosition = 0;
	public static int sFlag = 1;
	public static float sPo_int;
	public static float sScaleWidth;
	public static float sScaleHeight;
	private PageWidget mPageWidget;
	private String mFilenameString;
	private final int OPENTAG = Menu.FIRST + 1;
	private final int NEWTAG = Menu.FIRST + 2;
	private final int DELETETAG = Menu.FIRST + 3;
	final int BEGIN_SCROLL = 1; // start auto scroll
	final int END_SCROLL = 2; // end auto scroll
	final int STOP_SCROLL = 3; // stop auto scroll
	boolean mIsAutoScrolling = false;

	Bitmap mCurPageBitmap, mNextPageBitmap;
	Canvas mCurPageCanvas, mNextPageCanvas;
	BookPageFactory mPagefactory;
	TextView mTimeshow;

	Integer[] myImageIds = { R.drawable.bg, R.drawable.bg001, R.drawable.bg002,
			R.drawable.bg003, R.drawable.bg004, R.drawable.bg005,
			R.drawable.bg006, R.drawable.bg007, R.drawable.bg008,
			R.drawable.bg009, R.drawable.bg010, R.drawable.bg011 };

	Handler autoScrollHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			/* judge information */
			switch (msg.what) {
			case BEGIN_SCROLL:
				if (mIsAutoScrolling) {
					MotionEvent up = MotionEvent.obtain(
							SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_DOWN, 480, 800, 0);
					MotionEvent down = MotionEvent.obtain(
							SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
							480, 800, 0);
					onTouch(mPageWidget, up);
					onTouch(mPageWidget, down);
					autoScrollHandle
							.sendEmptyMessageDelayed(BEGIN_SCROLL, 5000);
				} else {
					autoScrollHandle.sendEmptyMessage(STOP_SCROLL);
				}
				break;
			case END_SCROLL:
				// scroll at end
				autoScrollHandle.removeMessages(STOP_SCROLL);
				autoScrollHandle.removeMessages(BEGIN_SCROLL);
				break;
			case STOP_SCROLL:
				// user stop scroll
				autoScrollHandle.removeMessages(END_SCROLL);
				autoScrollHandle.removeMessages(BEGIN_SCROLL);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mTimeshow = new TextView(this);
		mTimeshow.setGravity(Gravity.BOTTOM);
		handler.post(updateThread);
		mPageWidget = new PageWidget(this);
		setContentView(mPageWidget);
		mPageWidget.setOnTouchListener(this);

		Display d = this.getWindowManager().getDefaultDisplay();
		mCurPageBitmap = Bitmap.createBitmap(d.getWidth(), d.getHeight(),
				Bitmap.Config.ARGB_8888);// 32bytes
		mNextPageBitmap = Bitmap.createBitmap(d.getWidth(), d.getHeight(),
				Bitmap.Config.ARGB_8888);

		mCurPageCanvas = new Canvas(mCurPageBitmap);
		mNextPageCanvas = new Canvas(mNextPageBitmap);

		mPagefactory = new BookPageFactory(d.getWidth(), d.getHeight());

		Bundle bunde = this.getIntent().getExtras();
		mFilenameString = bunde.getString("fileName");
		sFlag = 1;

		int w = BitmapFactory.decodeResource(this.getResources(),
				myImageIds[sPosition]).getWidth();
		int h = BitmapFactory.decodeResource(this.getResources(),
				myImageIds[sPosition]).getHeight();

		sScaleWidth = ((float) d.getWidth() / w);
		sScaleHeight = ((float) d.getHeight() / h);
		sMatrix = new Matrix();
		sMatrix.postScale(sScaleWidth, sScaleHeight);
	}

	Handler handler = new Handler();
	Runnable updateThread = new Runnable() {
		public void run() {
			handler.postDelayed(updateThread, 1000);
			mPagefactory.onDraw(mCurPageCanvas);
			mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);
			mPageWidget.startAnimation(1000);
		}
	};

	/**
	 * Listen on the back key
	 */
	@Override
	public void onBackPressed() {
		updata();
		super.onBackPressed();
	}

	/**
	 * update the Last process of the reading
	 */
	public void updata() {
		if (ReaderDataBase.search(this, "LastReadProcess", mFilenameString) == 0) {
			ReaderDataBase.insertDataBase(this, "LastReadProcess",
					mFilenameString, BookPageFactory.sPercent);
		} else {
			ReaderDataBase.upData(this, "LastReadProcess", mFilenameString,
					BookPageFactory.sPercent);
		}
	}

	/**
	 * Listen on the volume key for paging
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			mPagefactory.onDraw(mCurPageCanvas);
			try {
				mPagefactory.prePage();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mPagefactory.onDraw(mNextPageCanvas);
			mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
			mPageWidget.startAnimation(1000);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mPagefactory.onDraw(mCurPageCanvas);
			try {
				mPagefactory.nextPage();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mPagefactory.onDraw(mNextPageCanvas);
			mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
			mPageWidget.startAnimation(1000);
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		handler.removeCallbacks(updateThread);
		super.onStop();
	}

	/**
	 * draw background on resume
	 */
	@Override
	protected void onResume() {
		super.onResume();

		mPagefactory.setBgBitmap(BitmapFactory.decodeResource(
				this.getResources(), myImageIds[sPosition]));
		try {
			mPagefactory.openbook(mFilenameString);
			mPagefactory.onDraw(mCurPageCanvas);
		} catch (IOException e1) {
			e1.printStackTrace();
			Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
		}
		mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);
		if (sFlag == 1) {
			try {
				mPagefactory.jumpPage(ReaderDataBase.search(this,
						"LastReadProcess", mFilenameString));

				auto();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * achieve the function of touch for reading
	 */
	public boolean onTouch(View v, MotionEvent e) {
		boolean ret = false;
		if (v == mPageWidget) {
			if (e.getAction() == MotionEvent.ACTION_DOWN) {
				mPageWidget.abortAnimation();
				mPageWidget.calcCornerXY(e.getX(), e.getY());
				mPagefactory.onDraw(mCurPageCanvas);
				if (mPageWidget.DragToRight()) {
					try {
						mPagefactory.prePage();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					if (mPagefactory.isfirstPage()) {
						Toast.makeText(ArchermindReaderActivity.this,
								getString(R.string.toast5), Toast.LENGTH_SHORT);
						return false;
					}
					mPagefactory.onDraw(mNextPageCanvas);
				} else {
					try {
						mPagefactory.nextPage();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					if (mPagefactory.islastPage())
						return false;
					mPagefactory.onDraw(mNextPageCanvas);
				}
				mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
			} else if (e.getAction() == MotionEvent.ACTION_UP) {
			} else if (e.getAction() == MotionEvent.ACTION_MOVE) {
			}
			ret = mPageWidget.doTouchEvent(e);
			return ret;
		}
		return false;
	}

	/**
	 * Create Options Menu
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = new MenuInflater(getApplicationContext());
		inflater.inflate(R.menu.menu, menu);
		SubMenu sub = menu.addSubMenu(getString(R.string.mark));
		sub.add(0, OPENTAG, Menu.NONE, getString(R.string.openmarklist));
		sub.add(0, NEWTAG, Menu.NONE, getString(R.string.addmark));
		sub.add(0, DELETETAG, Menu.NONE, getString(R.string.deleteallmark));
		return true;
	}

	/**
	 * Menu theme choice
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.changebg:
			sFlag = 0;
			Intent intent = new Intent();
			intent.setClass(ArchermindReaderActivity.this,
					ChangeBackground.class);
			Bundle bundle = new Bundle();
			bundle.putString("fileName", mFilenameString);
			intent.putExtras(bundle);
			startActivityForResult(intent, 0);
			break;
		case OPENTAG:
			openDialog();
			break;
		case NEWTAG:
			newDialog();
			break;
		case DELETETAG:
			deleteDialog();
			break;
		case R.id.jump:
			jumpPage();
			break;
		case R.id.autopage:
			mIsAutoScrolling = !mIsAutoScrolling;
			if (mIsAutoScrolling) {
				autoScrollHandle.sendEmptyMessage(BEGIN_SCROLL);
			} else {
				autoScrollHandle.sendEmptyMessage(STOP_SCROLL);
			}
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * open the list of tags
	 */
	private void openDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.allmark));
		final List<ReaderTags> listData = ReaderDataBase
				.getReaderDataBase(this);
		if (listData.size() > 0) {
			ListView list = new ListView(this);
			final MTagAdapter myAdapter = new MTagAdapter(this, listData);
			list.setAdapter(myAdapter);
			list.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Jump(arg2, listData, myAdapter);
				}
			});
			list.setOnItemLongClickListener(new OnItemLongClickListener() {

				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					deleteOneDialog(arg2, listData, myAdapter);
					return false;
				}
			});
			builder.setView(list);
		} else {
			TextView txt = new TextView(this);
			txt.setText(getString(R.string.nomark));
			txt.setPadding(10, 5, 0, 5);
			txt.setTextSize(16f);
			builder.setView(txt);
		}
		builder.setNegativeButton(getString(R.string.yes),
				new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * insert one tag when you are reading
	 */
	private void newDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.addmark));
		final EditText input = new EditText(this);
		input.setHint(getString(R.string.pleasemark));
		builder.setView(input);
		builder.setPositiveButton(getString(R.string.yes),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (input.getText().toString().trim().length() > 0) {
							addTag(input.getText().toString());
							dialog.dismiss();
							Toast.makeText(ArchermindReaderActivity.this,
									getString(R.string.successaddmark),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(ArchermindReaderActivity.this,
									getString(R.string.pleasemark),
									Toast.LENGTH_SHORT).show();
							newDialog();
						}
					}
				});
		builder.setNegativeButton(getString(R.string.no),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * delete one tag from the database
	 * 
	 * @param pos
	 * @param li
	 * @param adapter
	 */
	protected void deleteOneDialog(final int pos, final List<ReaderTags> li,
			final MTagAdapter adapter) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.warmhint));
		builder.setMessage(getString(R.string.changequestion3));
		builder.setPositiveButton(getString(R.string.yes),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteOneTag(li.get(pos));
						li.remove(pos);// Removed an option
						adapter.notifyDataSetChanged();// inform change
						dialog.dismiss();
						Toast.makeText(ArchermindReaderActivity.this,
								getString(R.string.successdelete),
								Toast.LENGTH_SHORT).show();
					}
				});

		builder.setNegativeButton(getString(R.string.no),
				new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * chose delete which tag
	 */
	private void deleteDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.warmhint));
		builder.setMessage(getString(R.string.changequestion2));
		builder.setPositiveButton(getString(R.string.yes),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteAllTag();
						dialog.dismiss();
						Toast.makeText(ArchermindReaderActivity.this,
								getString(R.string.successdelete),
								Toast.LENGTH_SHORT).show();
					}
				});
		builder.setNegativeButton(getString(R.string.no),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * jump to a page by the percent type in
	 */
	protected void jumpPage() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.txtjump));
		final SeekBar seek = new SeekBar(this);
		seek.setOnSeekBarChangeListener(this);
		seek.setMax(100);
		int p = (int) (BookPageFactory.sPercent * 100);
		seek.setProgress(p);
		builder.setView(seek);
		builder.setPositiveButton(getString(R.string.yes),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							int per = seek.getProgress();
							if (per >= 0.0 && per <= 100.0) {
								mPagefactory.jumpPage(per);
								dialog.dismiss();
								mPagefactory.onDraw(mCurPageCanvas);
								mPageWidget.setBitmaps(mCurPageBitmap,
										mNextPageBitmap);
								mPageWidget.startAnimation(1000);
								Toast.makeText(ArchermindReaderActivity.this,
										getString(R.string.successjump),
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(ArchermindReaderActivity.this,
										getString(R.string.hintjunp),
										Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(ArchermindReaderActivity.this,
									getString(R.string.hintjunp),
									Toast.LENGTH_SHORT).show();
						}
					}
				});
		builder.setNegativeButton(getString(R.string.no),
				new OnClickListener() {
					// @Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * the skip of tags with the skip-method
	 * 
	 * @param pos
	 * @param li
	 * @param adapter
	 */
	protected void Jump(final int pos, final List<ReaderTags> li,
			final MTagAdapter adapter) {

		JumpTag(li.get(pos));
	}

	/**
	 * get the percent for skip
	 * 
	 * @param tag
	 */
	private void JumpTag(ReaderTags tag) {
		if ((tag.filename).equals(mFilenameString)) {
			sPo_int = (tag.position * 100);
			try {
				mPagefactory.jumpPage(sPo_int);
				auto();
				Toast.makeText(ArchermindReaderActivity.this,
						getString(R.string.successjump), Toast.LENGTH_SHORT)
						.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(ArchermindReaderActivity.this,
					getString(R.string.notcurrenttext), Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * add one tag with the method in database
	 * 
	 * @param title
	 */
	protected void addTag(String title) {
		ReaderDataBase.insertDataBase(this, title, mFilenameString,
				BookPageFactory.sPercent);
	}

	/**
	 * delete one tag
	 * 
	 * @param tag
	 */
	protected void deleteOneTag(ReaderTags tag) {
		ReaderDataBase.deleteOne(this, tag._id);
	}

	/**
	 * delete all of the tags
	 */
	protected void deleteAllTag() {
		ReaderDataBase.deleteAll(this);
	}

	/**
	 * auto to the next page
	 */
	protected void auto() {
		mPagefactory.onDraw(mCurPageCanvas);
		mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
		mPageWidget.startAnimation(1000);
	}

	/**
	 * record the start of SeekBar
	 */
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	/**
	 * record the progress when stop touch the SeekBar
	 */
	public void onStopTrackingTouch(SeekBar seekBar) {
		Toast.makeText(ArchermindReaderActivity.this,
				seekBar.getProgress() + "%", Toast.LENGTH_SHORT).show();
	}

	/**
	 * change the progress with the SeekBar
	 */
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
	}

}