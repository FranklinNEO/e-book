/**
 * @Project:Archermindreader
 * @ClassName:SettingActivity
 * @Version 1.0
 * @Author shaojian.ni xue.xia xuegang.fu
 * @Update xuegang.fu
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.archermind.scrollreader.ReadBookActivity;

public class SettingActivity extends Activity {

	final int MENU_SAVE_AND_RETURN = 1;
	float mScrBrightness;
	public static String sThefile;
	public static int sDefault = Color.BLACK;
	public final static String READER_PREF = "cn.com.archermind.reader"; // mark
																			// reader
	public final static String FONT_COLOR = "tagFontColor"; // mark of font
															// color
	public final static String SCREEN_BRIGHTNESS = "tagScrBrightness"; // mark
																		// of
																		// screen
																		// brightness
	public final static String LANGUAGE = "taglanguage"; // mark coding
	SharedPreferences mSpSetting; // user settings
	String mFontColor;
	TextView mTvSettingPrev;
	TextView mTvlanguage;
	Spinner mSpFontColor;
	Spinner mSplanguage;
	SeekBar mSbFontSize;
	SeekBar mSbScrBrightness;
	ArrayAdapter<String> mAdapterFontColor;
	OnSeekBarChangeListener mOsbl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		mTvSettingPrev = (TextView) findViewById(R.id.settingtextviewsettingprev);
		mSbScrBrightness = (SeekBar) findViewById(R.id.settingseekbarscrbrightness);
		mSpFontColor = (Spinner) findViewById(R.id.settingspinnerfontcolor);

		initFromPref();

		Bundle bundle = this.getIntent().getExtras();
		sThefile = bundle.getString("fileName");
		if (bundle.containsKey(SCREEN_BRIGHTNESS)) {
			mScrBrightness = bundle.getFloat(SCREEN_BRIGHTNESS);
			Log.i("light", mScrBrightness * 10 + "");
			mSbScrBrightness.setProgress((int) (mScrBrightness * 10));
		}
		// Text color selector
		mAdapterFontColor = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		mAdapterFontColor
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAdapterFontColor.add(getString(R.string.defaultcolor));
		mAdapterFontColor.add(getString(R.string.colorblack));
		mAdapterFontColor.add(getString(R.string.colorwhite));
		mAdapterFontColor.add(getString(R.string.colorred));
		mAdapterFontColor.add(getString(R.string.colorgreen));
		mAdapterFontColor.add(getString(R.string.colorblue));
		mSpFontColor.setAdapter(mAdapterFontColor);
		mSpFontColor.setOnItemSelectedListener(new OnItemSelectedListener() {

			/**
			 * change the color of the font
			 */
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Spinner sp = (Spinner) parent;
				mFontColor = sp.getSelectedItem().toString();
				if (mFontColor == getString(R.string.colorwhite)) {
					mTvSettingPrev.setTextColor(Color.WHITE);
					sDefault = Color.WHITE;
				} else if (mFontColor == getString(R.string.colorblack)) {
					mTvSettingPrev.setTextColor(Color.BLACK);
					sDefault = Color.BLACK;
				} else if (mFontColor == getString(R.string.colorred)) {
					mTvSettingPrev.setTextColor(Color.RED);
					sDefault = Color.RED;
				} else if (mFontColor == getString(R.string.colorgreen)) {
					mTvSettingPrev.setTextColor(Color.GREEN);
					sDefault = Color.GREEN;
				} else if (mFontColor == getString(R.string.colorblue)) {
					mTvSettingPrev.setTextColor(Color.BLUE);
					sDefault = Color.BLUE;
				} else {
					mTvSettingPrev.setTextColor(sDefault);
				}
				mFontColor = mSpFontColor.getSelectedItem().toString();
			}

			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = mScrBrightness;
		getWindow().setAttributes(lp);
		mOsbl = new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				switch (seekBar.getId()) {
				case R.id.settingseekbarscrbrightness:
					WindowManager.LayoutParams lp = getWindow().getAttributes();
					lp.screenBrightness = (progress + 1) / 10.0f;
					getWindow().setAttributes(lp);
					break;
				}
			}

			/**
			 * start drag
			 */
			public void onStartTrackingTouch(SeekBar seekBar) {
				switch (seekBar.getId()) {
				case R.id.settingseekbarscrbrightness:
					break;
				}
			}

			/**
			 * end drag
			 */
			public void onStopTrackingTouch(SeekBar seekBar) {
				switch (seekBar.getId()) {
				case R.id.settingseekbarscrbrightness:
					mScrBrightness = (seekBar.getProgress() + 1) / 10.0f;
					break;
				}
			}

		};
		mSbScrBrightness.setOnSeekBarChangeListener(mOsbl);
	}

	/**
	 * set listen on the back key
	 */
	public void onBackPressed() {
		saveToPref();
		Intent intent = new Intent();
		intent.setClass(SettingActivity.this, ReadBookActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("fileName", sThefile);
		bundle.putFloat(SCREEN_BRIGHTNESS, mScrBrightness);
		intent.putExtras(bundle);
		startActivity(intent);
		super.onBackPressed();
	}

	public void initFromPref() {
		mSpSetting = getSharedPreferences(READER_PREF, 0);
		mScrBrightness = mSpSetting.getFloat(SCREEN_BRIGHTNESS, 1.0f);
	}

	public void saveToPref() {
		mSpSetting = getSharedPreferences(READER_PREF, 0);
		SharedPreferences.Editor ed = mSpSetting.edit();
		ed.putString(FONT_COLOR, mFontColor);
		ed.putFloat(SCREEN_BRIGHTNESS, mScrBrightness);
		ed.commit();
	}
}
