package com.archermind.scrollreader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.text.ClipboardManager;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.archermind.main.R;
import com.archermind.main.SettingActivity;
import com.archermind.mark.MTagAdapter;
import com.archermind.mark.ReaderDataBase;
import com.archermind.mark.ReaderTags;

public class ReadBookActivity extends Activity implements OnClickListener,
		OnSeekBarChangeListener, OnScrollListener {
	public static String CODE;
	final int REQUST_CODE_GOTO_BOOKMARK = 1;
	public static int mSize = 20;
	TextView mTvMain; // main control
	String mStrSelection = ""; // the string user selected
	String mStrTxt = ""; // use to show text string
	private String mFileNameKey = "fileName"; // full path of file
	public final static String SCREEN_BRIGHTNESS = "tagScrBrightness";
	int mPosition; // local read position,take the first line
	int m = 0; // position of book marks
	public static int POS;
	final int BUFFER_SIZE = 1024 * 3;
	private static int mMbBufLen = 0;
	private static int mMbBufBegin = 0;
	private MappedByteBuffer mMbBuf = null;
	final int SCROLL_STEP = 1; // the step of auto scroll
	final int BEGIN_SCROLL = 1; // start auto scroll
	final int END_SCROLL = 2; // end auto scroll
	final int STOP_SCROLL = 3; // stop auto scroll
	final int MENU_BOOK = Menu.FIRST;
	final int MENU_JUMP = Menu.FIRST + 1;
	final int MENU_SETTING = Menu.FIRST + 2;
	final int MENU_BACKGROUND = Menu.FIRST + 3;
	private final int OPEN_TAG = Menu.FIRST + 4;
	private final int NEW_TAG = Menu.FIRST + 5;
	private final int DELETE_TAG = Menu.FIRST + 6;

	// private ListView listView = null;
	// private ArrayList<String> listdata = new ArrayList<String>();
	// private MyAdapter adapter = null;
	// private int lastVisibleIndex;

	final int DIALOG_AFTER_SELECTION = 4;
	final int DIALOG_GET_SEARCH_KEY_WORD = 5;
	// private TextView mTime;
	private TextView mPercent;
	float mPer = 0.0f;
	float mScreenLight = 1.0f;
	boolean mIsAutoScrolling = false;
	Integer[] myImageIds = { R.drawable.bg, R.drawable.bg001, R.drawable.bg002,
			R.drawable.bg003, R.drawable.bg004, R.drawable.bg005,
			R.drawable.bg006, R.drawable.bg007, R.drawable.bg008,
			R.drawable.bg009, R.drawable.bg010, R.drawable.bg011,
			R.drawable.ic_launcher };
	Button BtnAutoScroll;
	private ImageButton mButton1;
	private ImageButton mButton2;
	private int height;
	/**
	 * handler of auto read
	 */
	Handler autoScrollHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			/* judge information */
			switch (msg.what) {
			case BEGIN_SCROLL:
				if (mTvMain.getScrollY() >= mTvMain.getLineCount()
						* mTvMain.getLineHeight() - mTvMain.getHeight()) {
					mTvMain.scrollTo(0,
							mTvMain.getLineCount() * mTvMain.getLineHeight()
									- mTvMain.getHeight());
					autoScrollHandle.sendEmptyMessage(END_SCROLL);
				} else {
					// scroll by step
					mTvMain.scrollTo(0, mTvMain.getScrollY() + SCROLL_STEP);
					autoScrollHandle.sendEmptyMessageDelayed(BEGIN_SCROLL, 10);
				}
				break;
			case END_SCROLL:
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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.readbook);
		FileInputStream inStream = null;
		ByteArrayOutputStream outStream = null;
		try {
			inStream = this.openFileInput("userInfo.txt");
			outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}
			String content = outStream.toString();
			Log.v("content", content);
			height = Integer.valueOf(content).intValue();
			outStream.close();
			inStream.close();
		} catch (IOException ex) {
		}
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		mButton1 = (ImageButton) findViewById(R.id.fontsize_small);
		mButton2 = (ImageButton) findViewById(R.id.fontsize_big);
		mButton1.setOnClickListener(this);
		mButton2.setOnClickListener(this);

		// listView = (ListView) findViewById(R.id.read_list);
		// adapter = new MyAdapter(ReadBookActivity.this);
		// listView.setOnScrollListener(this);

		mTvMain = (TextView) this.findViewById(R.id.viewtxt_main_view);
		mPercent = (TextView) this.findViewById(R.id.percent);
		// setpercent();

		try {

			Bundle b = getIntent().getExtras();
			String str = b.getString(mFileNameKey);
			if (b.containsKey(SCREEN_BRIGHTNESS)) {

				mScreenLight = b.getFloat(SCREEN_BRIGHTNESS);
				Log.i("screen", mScreenLight + "");
				lp.screenBrightness = mScreenLight;

			}
			mFileNameKey = str; // save
			showText(mFileNameKey);
			// listdata.add(mStrTxt);
			// listView.setAdapter(adapter);

			Log.d("LogString", mStrTxt);
			mTvMain.setText(mStrTxt);
			// if (HaveRead()) {
			// Return();
			// setpercent();
			// }
			FileReader fr = new FileReader(str);
			char[] buf = new char[BUFFER_SIZE];
			try {
				fr.read(buf);
			} catch (IOException e) {
			}

			// byte[] bytes = new byte[buf.length];
			// String strDst = new String(bytes, 0, buf.length, DecType);
			// mStrTxt = new String(buf);

			mTvMain.setTextColor(SettingActivity.sDefault);
			mTvMain.setTextSize(mSize);
			mTvMain.setCursorVisible(false);
			mTvMain.setMovementMethod(ScrollingMovementMethod.getInstance());
			getWindow().setAttributes(lp);

		} catch (FileNotFoundException e) {

		}
		// set onClickListener
		Button BtnPrePage = (Button) this.findViewById(R.id.viewtxt_pre_button);
		BtnPrePage.setOnClickListener(this);
		Button BtnNextPage = (Button) this
				.findViewById(R.id.viewtxt_next_button);
		BtnNextPage.setOnClickListener(this);
		Button BtnAutoScroll = (Button) this
				.findViewById(R.id.viewtxt_auto_scroll_button);
		BtnAutoScroll.setOnClickListener(this);

	}

	// public final class ViewHolder {
	// public TextView dataText;
	// }
	//
	// private class MyAdapter extends BaseAdapter {
	// private LayoutInflater inflater;
	//
	// public MyAdapter(Context context) {
	// this.inflater = LayoutInflater.from(context);
	// }
	//
	// @Override
	// public int getCount() {
	// return listdata.size();
	// }
	//
	// @Override
	// public Object getItem(int position) {
	// return listdata.get(position);
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// return position;
	// }
	//
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// ViewHolder holder = null;
	// if (convertView == null) {
	// convertView = inflater.inflate(R.layout.listdata_item, null);
	// holder = new ViewHolder();
	// holder.dataText = (TextView) convertView
	// .findViewById(R.id.listitem);
	// convertView.setTag(holder);
	// } else {
	// holder = (ViewHolder) convertView.getTag();
	// }
	// holder.dataText.setText(listdata.get(position));
	// return convertView;
	// }
	//
	// }

	/**
	 * set volume key to page up and down
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (mTvMain.getScrollY() <= mTvMain.getHeight()) {
				mTvMain.scrollTo(0, 0);
			} else {
				mTvMain.scrollTo(0, mTvMain.getScrollY() - mTvMain.getHeight());
			}
			setpercent();
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (mTvMain.getScrollY() >= mTvMain.getLineCount()
					* mTvMain.getLineHeight() - mTvMain.getHeight() * 2) {
				mTvMain.scrollTo(0,
						mTvMain.getLineCount() * mTvMain.getLineHeight()
								- mTvMain.getHeight());
			} else {
				mTvMain.scrollTo(0, mTvMain.getScrollY() + mTvMain.getHeight());
			}
			setpercent();
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * fresh the size of text
	 */
	private void fresh() {
		mTvMain.setTextSize(mSize);
		Log.i("fontsize", mSize + "");
		Log.i("CODE", CODE + "");
	}

	/**
	 * judge if have been read
	 * 
	 * @return
	 */
	public boolean HaveRead() {
		if (ReaderDataBase.search(this, "LastReadProcess", mFileNameKey) != 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * set to the position last time read
	 */
	public void Return() {
		Log.d("line", mTvMain.getLineCount() + "");
		int mark = (int) (ReaderDataBase.search(this, "LastReadProcess",
				mFileNameKey) * height / 100);
		Log.d("lastread", mark + "");

		mTvMain.scrollTo(0, mark);
		setpercent();
		Log.i("none", mPosition + "");
		Log.e("REALPOS_RESUME", "REAL_POS " + mPosition);

	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		setpercent();
		return super.onTrackballEvent(event);
	}

	/**
	 * set the position before quit
	 */
	private void updata() {
		mPosition = mTvMain.getScrollY();
		Log.d("p", mPosition + "");
		if (ReaderDataBase.search(this, "LastReadProcess", mFileNameKey) == 0) {
			Log.d("insert", "1");
			ReaderDataBase
					.insertDataBase(
							this,
							"LastReadProcess",
							mFileNameKey,
							(float) (mPosition * 1.0 / ((mTvMain
									.getLineHeight() * mTvMain.getLineCount()))));
			saveHeightInfo((mTvMain.getLineHeight() * mTvMain.getLineCount()));
			Log.d("Position",
					(float) (mPosition * 1.0 / ((mTvMain.getLineHeight() * mTvMain
							.getLineCount()))) + "");
		} else {
			ReaderDataBase
					.upData(this,
							"LastReadProcess",
							mFileNameKey,
							(float) (mPosition * 1.0 / ((mTvMain
									.getLineHeight() * mTvMain.getLineCount()))));
			saveHeightInfo((mTvMain.getLineHeight() * mTvMain.getLineCount()));
			Log.d("Position",
					(float) (mPosition * 1.0 / ((mTvMain.getLineHeight() * mTvMain
							.getLineCount()))) + "");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d("resume", "1");
		this.getWindow().setBackgroundDrawableResource(myImageIds[POS]);
		Log.i("POS", POS + "");
		Log.e("REALPOS_RESUME", "REAL_POS " + mPosition);
		Layout l = mTvMain.getLayout();

		if (null != l) {
			int line = l.getLineForOffset(mPosition);
			float sy = l.getLineBottom(line);
			Log.d("height", "" + sy);
			// mTvMain.scrollTo(0, (int) sy);
			// setpercent();
			Log.i("none", mPosition + "");
			Log.e("REALPOS_RESUME", "REAL_POS " + mPosition);
		}
	}

	/**
	 * set the current percent
	 */
	protected void setpercent() {
		mPer = (float) (mMbBufBegin / mMbBufLen * 100);
		// if (mTvMain.getLineCount() == 0) {
		// mPer = 0;
		// } else {
		// mPer = (float) ((mTvMain.getScrollY() + mTvMain.getHeight()) * 1000
		// / (mTvMain.getLineCount() * mTvMain.getLineHeight()) / 10.0);
		// if (mPer > 100) {
		// mPer = 100;
		// }
		// }
		mPercent.setText(mPer + "%");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("pause", "1");
	}

	@Override
	protected void onStop() {
		updata();
		super.onStop();
		Log.d("stop", "1");
	}

	/**
	 * set onclick method
	 */
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.fontsize_big:

			mSize = mSize + 2;
			if (mSize >= 50) {
				mSize = 50;

				mButton2.setBackgroundResource(R.drawable.mm_zoomin_btn_disabled);

			} else if (mSize >= 8 && mSize <= 50) {

				mButton1.setBackgroundResource(R.drawable.zoomout);
			}
			Log.i("fontsize", mSize + "");
			fresh();
			break;
		case R.id.fontsize_small:

			mSize = mSize - 2;
			if (mSize <= 8) {
				mSize = 8;
				mButton1.setBackgroundResource(R.drawable.mm_zoomout_btn_disabled);

			} else if (mSize >= 8 && mSize <= 50) {
				mButton2.setBackgroundResource(R.drawable.zoomin);
			}
			Log.i("fontsize", mSize + "");
			fresh();
			break;
		case R.id.viewtxt_pre_button:
			if (mTvMain.getScrollY() <= mTvMain.getHeight()) {

				mTvMain.scrollTo(0, 0);
			} else {

				mTvMain.scrollTo(0, mTvMain.getScrollY() - mTvMain.getHeight());
			}

			Log.e("", "LINEHEIGHT = " + mTvMain.getLineHeight());
			break;
		case R.id.viewtxt_next_button:
			if (mTvMain.getScrollY() >= mTvMain.getLineCount()
					* mTvMain.getLineHeight() - mTvMain.getHeight() * 2) {

				mTvMain.scrollTo(0,
						mTvMain.getLineCount() * mTvMain.getLineHeight()
								- mTvMain.getHeight());
			} else {

				mTvMain.scrollTo(0, mTvMain.getScrollY() + mTvMain.getHeight());
			}

			Log.e("",
					"LINECOUNT*LINEHEIGHT = "
							+ (mTvMain.getLineCount() * mTvMain.getLineHeight() - mTvMain
									.getHeight()));
			Log.e("", "SCROLLY = " + mTvMain.getScrollY());
			Log.e("", "TVHEIGHT = " + mTvMain.getHeight());
			break;
		case R.id.viewtxt_auto_scroll_button:
			Log.d("scroll", "click");
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
	}

	/**
	 * main menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		SubMenu sub = menu.addSubMenu(getString(R.string.mark));
		sub.add(0, OPEN_TAG, Menu.NONE, getString(R.string.openmarklist));
		sub.add(0, NEW_TAG, Menu.NONE, getString(R.string.addmark));
		sub.add(0, DELETE_TAG, Menu.NONE, getString(R.string.deleteallmark));

		menu.add(0, MENU_JUMP, 0, getString(R.string.jump));
		menu.add(0, MENU_BACKGROUND, 0, getString(R.string.changebg));
		menu.add(0, MENU_SETTING, 0, getString(R.string.set));

		return true;
	}

	/**
	 * set the listener of menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {

		case MENU_JUMP:
			jumpPage();
			break;

		case MENU_SETTING:
			Intent intent = new Intent();
			intent.setClass(ReadBookActivity.this, SettingActivity.class);
			Bundle bundle1 = new Bundle();
			bundle1.putString("fileName", mFileNameKey);
			bundle1.putFloat(SCREEN_BRIGHTNESS, mScreenLight);

			intent.putExtras(bundle1);
			startActivity(intent);

			updata();
			this.finish();

			break;
		case MENU_BACKGROUND:
			off();
			Intent intent1 = new Intent();
			intent1.setClass(ReadBookActivity.this, BackGround.class);
			Bundle bundle = new Bundle();
			bundle.putString("fileName", mFileNameKey);
			intent1.putExtras(bundle);
			startActivityForResult(intent1, 0);
			updata();
			this.finish();
			break;
		case OPEN_TAG:
			off();
			openDialog();
			break;
		case NEW_TAG:
			off();
			newDialog();
			break;
		case DELETE_TAG:
			deleteDialog();
			break;
		default:
			break;
		}

		return false;
	}

	/**
	 * set position
	 */
	public void off() {
		Layout L = mTvMain.getLayout();
		if (null != L) {
			int Line = L.getLineForVertical(mTvMain.getScrollY());
			if (Line != 0) {
				int Off = L.getOffsetForHorizontal(Line - 1, 0);
				mPosition = Off;
			} else {
				int Off = L.getOffsetForHorizontal(Line, 0);
				mPosition = Off;
			}
		}
	}

	/**
	 * delete a tag
	 */
	private void deleteDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.warmhint));
		builder.setMessage(getString(R.string.changequestion2));
		builder.setPositiveButton(getString(R.string.yes),
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteAllTag();
						dialog.dismiss();
						Toast.makeText(ReadBookActivity.this,
								getString(R.string.successdelete),
								Toast.LENGTH_SHORT).show();
					}
				});
		builder.setNegativeButton(getString(R.string.no),
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * delete all tag
	 */
	protected void deleteAllTag() {
		ReaderDataBase.deleteAll(this);
	}

	/**
	 * create tag
	 */
	private void newDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.addmark));
		final EditText input = new EditText(this);
		input.setHint(getString(R.string.pleasemark));
		builder.setView(input);
		builder.setPositiveButton("yes",
				new android.content.DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						if (input.getText().toString().trim().length() > 0) {
							addTag(input.getText().toString());
							dialog.dismiss();
							Toast.makeText(ReadBookActivity.this,
									getString(R.string.successaddmark),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(ReadBookActivity.this,
									getString(R.string.pleasemark),
									Toast.LENGTH_SHORT).show();
							newDialog();
						}
					}
				});
		builder.setNegativeButton("no",
				new android.content.DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * add tag
	 * 
	 * @param title
	 */
	protected void addTag(String title) {
		ReaderDataBase.insertDataBase(this, title, mFileNameKey,
				(float) (mPosition * 1.0 / ((mTvMain.getLineHeight() * mTvMain
						.getLineCount()))));
		saveHeightInfo((mTvMain.getLineHeight() * mTvMain.getLineCount()));
	}

	/**
	 * show all tags
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
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * jump to the tag
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
	 * jump tag
	 * 
	 * @param tag
	 */
	private void JumpTag(ReaderTags tag) {
		if ((tag.filename).equals(mFileNameKey)) {
			int mark = (int) ((tag.position) * mTvMain.getLineCount() * mTvMain
					.getLineHeight());
			Layout l = mTvMain.getLayout();
			if (null != l) {
				// goto the position of book marks
				int line = l.getLineForOffset(mark);
				float sy = l.getLineBottom(line);
				mTvMain.scrollTo(0, (int) sy);
				Log.e("REALPOS_RES", "REAL_POS " + mark);
				Toast.makeText(ReadBookActivity.this,
						getString(R.string.successjump), Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			Toast.makeText(ReadBookActivity.this,
					getString(R.string.notcurrenttext), Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * delete one tag
	 * 
	 * @param pos
	 * @param listData
	 * @param adapter
	 */
	protected void deleteOneDialog(final int pos,
			final List<ReaderTags> listData, final MTagAdapter adapter) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.warmhint));
		builder.setMessage(getString(R.string.changequestion3));
		builder.setPositiveButton(getString(R.string.yes),
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteOneTag(listData.get(pos));
						listData.remove(pos);// Removed an option
						adapter.notifyDataSetChanged();// inform change
						dialog.dismiss();
						Toast.makeText(ReadBookActivity.this,
								getString(R.string.successdelete),
								Toast.LENGTH_SHORT).show();
					}
				});

		builder.setNegativeButton(getString(R.string.no),
				new android.content.DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
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
	 * chose what to do after selection
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		// dialog of MSM
		case DIALOG_AFTER_SELECTION:
			return new AlertDialog.Builder(ReadBookActivity.this)
					.setIcon(android.R.drawable.ic_dialog_info)
					// .setTitle("welcome")
					.setMessage(R.string.toast2)
					.setPositiveButton(
							getString(R.string.sendSMS),
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface a0, int a1) {
									Uri smsToUri = Uri.parse("smsto://");
									Intent mIntent = new Intent(
											Intent.ACTION_SENDTO, smsToUri);

									// Copied to the clipboard,then call system
									// of message program
									ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
									clipboard.setText(mStrSelection);
									startActivity(mIntent);

									Toast.makeText(ReadBookActivity.this,
											getString(R.string.toast1),
											Toast.LENGTH_LONG).show();

									// mTvMain.clearSelection();
								}
							})
					.setNeutralButton(
							getString(R.string.call),
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface a0, int a1) {
									// the number is legal?
									if (PhoneNumberUtils
											.isGlobalPhoneNumber(mStrSelection)) {
										Intent i = new Intent(
												Intent.ACTION_CALL,
												Uri.parse("tel://"
														+ mStrSelection));
										startActivity(i);
									} else {
										Toast.makeText(
												ReadBookActivity.this,
												getString(R.string.illegaltelephonenumber),
												Toast.LENGTH_LONG).show();
									}
									Log.e("", "NUM = " + mStrSelection);
									// mTvMain.clearSelection();
								}
							})
					.setNegativeButton(
							getString(R.string.no),
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface a0, int a1) {
									// mTvMain.clearSelection();
								}
							}).create();
		default:
			return null;
		}
	}

	/**
	 * set code of read
	 * 
	 * @param fileNameKey
	 * @return
	 */
	public String convertCode(String fileNameKey) {
		File file = new File(fileNameKey);
		// BufferedReader reader;
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream in = new BufferedInputStream(fis);
			in.mark(4);
			byte[] first3bytes = new byte[3];
			in.read(first3bytes);
			in.reset();
			if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
					&& first3bytes[2] == (byte) 0xBF) {
				CODE = "utf-8";
			} else if (first3bytes[0] == (byte) 0xFF
					&& first3bytes[1] == (byte) 0xFE) {
				CODE = "unicode";
			} else if (first3bytes[0] == (byte) 0xFE
					&& first3bytes[1] == (byte) 0xFF) {
				CODE = "utf-16be";
			} else if (first3bytes[0] == (byte) 0xFF
					&& first3bytes[1] == (byte) 0xFF) {
				CODE = "utf-16le";
			} else {
				CODE = "GB2312";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return CODE;

	}

	/**
	 * show the text of file
	 * 
	 * @param fileString
	 * @return
	 */
	public String showText(String fileString) {
		// convertCode(mFileNameKey);
		// if (mFileNameKey == null) {
		// this.finish();
		// }
		// BufferedReader reader = null;
		// File file = new File(mFileNameKey);
		// FileInputStream fis;
		// try {
		// fis = new FileInputStream(file);
		// BufferedInputStream in = new BufferedInputStream(fis);
		// reader = new BufferedReader(new InputStreamReader(in, CODE));
		// in.reset();
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// try {
		// String str = reader.readLine();
		// while (str != null) {
		// mStrTxt = mStrTxt + str + "\n";
		// str = reader.readLine();
		// }
		// reader.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// return mStrTxt;
		int ParagraphSize = 5000;
		File file = new File(fileString);
		long lLen = file.length();
		mMbBufLen = (int) lLen;
		Log.d("showlength", lLen + "");
		try {
			int i = 0;
			if ((lLen - mMbBufBegin) > 5000) {
				ParagraphSize = 5000;
			} else {
				ParagraphSize = (int) lLen - mMbBufBegin;
			}
			if (mMbBufBegin < lLen) {

				mMbBuf = new RandomAccessFile(file, "r").getChannel().map(
						FileChannel.MapMode.READ_ONLY, 0, ParagraphSize);
				Log.e("Begin", mMbBufBegin + "");
				mMbBufBegin += ParagraphSize;
				byte[] buf = new byte[ParagraphSize];
				for (i = 0; i < (ParagraphSize); i++) {
					buf[i] = mMbBuf.get(i);
				}
				String strParagraph = new String(buf, convertCode(mFileNameKey));
				Log.d("showtext", strParagraph);
				mStrTxt = strParagraph;
			} else {
				mStrTxt = "";
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("showtext", mStrTxt);
		return mStrTxt;
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
		int p = (int) (mPer);
		seek.setProgress(p);
		builder.setView(seek);
		builder.setPositiveButton(getString(R.string.yes),
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							int per = seek.getProgress();
							if (per >= 0.0 && per <= 100.0) {
								mTvMain.scrollTo(
										0,
										(per * mTvMain.getLineCount()
												* mTvMain.getLineHeight() / 100 - mTvMain
												.getHeight()) + 1);

								dialog.dismiss();
								setpercent();
								Toast.makeText(ReadBookActivity.this,
										getString(R.string.successjump),
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(ReadBookActivity.this,
										getString(R.string.hintjunp),
										Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(ReadBookActivity.this,
									getString(R.string.hintjunp),
									Toast.LENGTH_SHORT).show();
						}
					}
				});
		builder.setNegativeButton(getString(R.string.no),
				new android.content.DialogInterface.OnClickListener() {
					// @Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();

	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

	}

	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		Toast.makeText(ReadBookActivity.this, seekBar.getProgress() + "%",
				Toast.LENGTH_SHORT).show();

	}

	public void saveHeightInfo(int h) {
		try {
			FileOutputStream outStream = this.openFileOutput("userInfo.txt",
					Context.MODE_PRIVATE);
			String content = h + "";
			outStream.write(content.getBytes());
			outStream.close();
		} catch (IOException ex) {

		}
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	// @Override
	// public void onScroll(AbsListView view, int firstVisibleItem,
	// int visibleItemCount, int totalItemCount) {
	// // TODO Auto-generated method stub
	// lastVisibleIndex = firstVisibleItem + visibleItemCount;
	// }
	//
	// @Override
	// public void onScrollStateChanged(AbsListView view, int scrollState) {
	// // TODO Auto-generated method stub
	// if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
	// && lastVisibleIndex == adapter.getCount()) {
	// showText(mFileNameKey);
	// new Thread() {
	// @Override
	// public void run() {
	// // /获取更多数据
	// if (mStrTxt != "") {
	// listdata.add(mStrTxt);
	// }
	// }
	// }.start();
	// }
	// adapter.notifyDataSetChanged();
	// setpercent();
	// }
}
