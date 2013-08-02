/**
 * @Project:Archermindreader
 * @ClassName:MainActivity
 * @Version 1.0
 * @Author shaojian.ni xue.xia
 * @Update xuegang.fu
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.archermind.scrollreader.ReadBookActivity;

public class MainActivity extends Activity implements OnClickListener {
	public static ArrayList<File> Filelist = new ArrayList<File>();
	private ArrayList<String> Urilist = new ArrayList<String>(
			(Arrays.asList("http://g.xxsy.net")));
	private ArrayList<String> SiteNamelist = new ArrayList<String>(
			(Arrays.asList("潇湘书院")));
	private Bundle mBundle;
	private String mFileNameKey = "fileName";
	private String mUriKey = "uriName";
	private GridView bookShelf;
	private GridView gv;
	private SlidingDrawer sd;
	private Button iv;
	private List<ResolveInfo> apps;
	public static int pos;
	private Button sb;
	private RelativeLayout rl = null;
	public PopupWindow mPopupWindow;
	private View funclayout = null;
	public static boolean READ_MODE = false;

	/**
	 * set the main UI
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Filelist.addAll(Splashing.mFileNameLists);

		rl = (RelativeLayout) findViewById(R.id.head);
		sb = (Button) rl.findViewById(R.id.btn_rightTop);
		sb.setOnClickListener(this);
		bookShelf = (GridView) findViewById(R.id.bookShelf);
		ShlefAdapter adapter = new ShlefAdapter();
		bookShelf.setAdapter(adapter);
		bookShelf.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				pos = arg2;
				if (arg2 >= Filelist.size()) {

				} else {
					if (READ_MODE) {
						mBundle = new Bundle();
						Intent intent = new Intent(MainActivity.this,
								ReadBookActivity.class);
						mBundle.putString(mFileNameKey, Filelist.get(pos)
								.toString());
						intent.putExtras(mBundle);
						startActivityForResult(intent, 0);
					} else {
						mBundle = new Bundle();
						Intent intent = new Intent(MainActivity.this,
								ArchermindReaderActivity.class);
						mBundle.putString(mFileNameKey, Filelist.get(pos)
								.toString());
						intent.putExtras(mBundle);
						startActivityForResult(intent, 0);
					}
				}
			}
		});
		loadApps();
		gv = (GridView) findViewById(R.id.allApps);
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (arg2 < Urilist.size()) {
					mBundle = new Bundle();
					mBundle.putString(mUriKey, Urilist.get(arg2));
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, WebReading.class);
					intent.putExtras(mBundle);
					startActivity(intent);
				}
			}
		});
		sd = (SlidingDrawer) findViewById(R.id.sliding);
		iv = (Button) findViewById(R.id.imageViewIcon);
		gv.setAdapter(new GridAdapter(this));
		sd.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener()// 开抽屉
		{
			@Override
			public void onDrawerOpened() {
				iv.setBackgroundResource(R.drawable.shake_report_dragger_down_normal);// 响应开抽屉事件
				// ，把图片设为向下的
			}
		});
		sd.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
			@Override
			public void onDrawerClosed() {
				iv.setBackgroundResource(R.drawable.shake_report_dragger_up_normal);// 响应关抽屉事件
			}
		});
	}

	class ShlefAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return Filelist.size() + 5;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View contentView, ViewGroup arg2) {
			// TODO Auto-generated method stub

			contentView = LayoutInflater.from(getApplicationContext()).inflate(
					R.layout.item1, null);

			TextView view = (TextView) contentView
					.findViewById(R.id.imageView1);
			if (Filelist.size() > position) {
				view.setText(getFileName(Filelist.get(position).toString()));
				view.setBackgroundResource(R.drawable.cover_txt);
			} else {
				view.setBackgroundResource(R.drawable.cover_txt);
				view.setClickable(false);
				view.setVisibility(View.INVISIBLE);
			}
			return contentView;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("你确定退出吗？")
					.setCancelable(false)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							})
					.setNegativeButton("返回",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public static String getFileName(String strFilePath) {
		int nPos = strFilePath.lastIndexOf("/");

		if (nPos < 0)
			return null;

		String strFileName = strFilePath.substring(nPos + 1);
		String[] splitStr = strFileName.split("\\.");
		return splitStr[0];
	}

	private void loadApps() {
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		apps = getPackageManager().queryIntentActivities(intent, 0);
	}

	public final class ViewHolder {
		public TextView GridText;
		public ImageView GridImage;
	}

	public class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public GridAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return Urilist.size() + 1;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.gridview_item, null);
				holder = new ViewHolder();
				holder.GridText = (TextView) convertView
						.findViewById(R.id.gridTV);
				holder.GridImage = (ImageView) convertView
						.findViewById(R.id.gridIV);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (position < Urilist.size()) {
				holder.GridText.setText(SiteNamelist.get(position));
				holder.GridImage
						.setBackgroundResource(R.drawable.albumshareurl_icon);
			} else if (position == (Urilist.size())) {
				holder.GridText.setText("添加");
				holder.GridImage
						.setBackgroundResource(R.drawable.app_panel_add_icon_pressed);
			}

			return convertView;
		}
		// Intent it = new Intent(Intent.ACTION_VIEW, Uri
		// .parse("http://g.xxsy.net"));
		//
		// it.setClassName("com.android.browser",
		// "com.android.browser.BrowserActivity");
		//
		// startActivity(it);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_rightTop:

			if (mPopupWindow == null) {
				funclayout = getLayoutInflater()
						.inflate(R.layout.control, null);
				mPopupWindow = new PopupWindow(funclayout,
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			}
			mPopupWindow.setOutsideTouchable(true); // 设置popwindow外的区域可点击
			mPopupWindow.setFocusable(true);
			mPopupWindow.setTouchable(true);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.showAtLocation(findViewById(R.id.layout_main),
					Gravity.RIGHT | Gravity.TOP, 0, (int) this.getResources()
							.getDimension(R.dimen.titleheightsize));

			Button func1 = (Button) funclayout.findViewById(R.id.fn1);

			func1.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mPopupWindow.dismiss();
					READ_MODE = false;
					Toast.makeText(MainActivity.this, "切换为翻页阅读模式",
							Toast.LENGTH_SHORT).show();

				}
			});

			Button func2 = (Button) funclayout.findViewById(R.id.fn2);

			func2.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mPopupWindow.dismiss();
					READ_MODE = true;
					Toast.makeText(MainActivity.this, "切换为滚屏阅读模式",
							Toast.LENGTH_SHORT).show();
				}
			});
			break;
		default:
			break;
		}
	}

}
