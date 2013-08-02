/**
 * @Project:Archermindreader
 * @ClassName:ChangeBackground
 * @Version 1.0
 * @Author shaojian.ni
 * @Update xuegang.fu
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;


public class ChangeBackground extends Activity {
	private Gallery mMyGallery;
	private ImageAdapter mMyImageAdapter;
	public String mFilenameString;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changebg);
		final Integer[] mMyImageIds = { R.drawable.bg, R.drawable.bg001,
				R.drawable.bg002, R.drawable.bg003, R.drawable.bg004,
				R.drawable.bg005, R.drawable.bg006, R.drawable.bg007,
				R.drawable.bg008, R.drawable.bg009, R.drawable.bg010,
				R.drawable.bg011 };

		Bundle bundle = this.getIntent().getExtras();
		mFilenameString = bundle.getString("fileName");
		mMyImageAdapter = new ImageAdapter(this, mMyImageIds);
		mMyGallery = (Gallery) findViewById(R.id.myGallery);// The graph showing
															// the way to set
															// gallery
		mMyGallery.setAdapter(mMyImageAdapter);

		/**
		 * get the position of the resource, then change the background in
		 * ArchermindReaderActivity
		 */
		mMyGallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				Resources resources = getBaseContext().getResources();
				Drawable d = resources.getDrawable(mMyImageIds[position]);
				new AlertDialog.Builder(ChangeBackground.this)
						.setTitle(getString(R.string.bgphoto))
						.setMessage(getString(R.string.changequestion1))
						.setPositiveButton(getString(R.string.yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										try {
											ArchermindReaderActivity.sPosition = position;
											ChangeBackground.this.finish();
											if (position >= 0) {
												Toast.makeText(
														ChangeBackground.this,
														getString(R.string.bgischange),
														Toast.LENGTH_LONG)
														.show();
											} else {
												Toast.makeText(
														ChangeBackground.this,
														getString(R.string.bgisnotchange),
														Toast.LENGTH_LONG)
														.show();
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								})
						.setNegativeButton(getString(R.string.no),
								new OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										Toast.makeText(ChangeBackground.this,
												getString(R.string.no),
												Toast.LENGTH_SHORT).show();
									}
								}).show().getWindow().setBackgroundDrawable(d);
			}
		});
	}

	/**
	 * set the ImageAdapter for the background resource
	 * 
	 * @author archermind
	 * 
	 */
	public class ImageAdapter extends BaseAdapter {

		int mMyGalleryItemBackground;
		private Context mMyContext;
		private Integer[] mMyImageIds;

		/**
		 * get the id of the resource
		 * 
		 * @param c
		 * @param aid
		 */
		public ImageAdapter(Context c, Integer[] aid) {
			mMyContext = c;
			mMyImageIds = aid;
			TypedArray a = obtainStyledAttributes(R.styleable.Gallery);
			mMyGalleryItemBackground = a.getResourceId(
					R.styleable.Gallery_android_galleryItemBackground, 0);
			a.recycle();
		}

		public int getCount() {
			return mMyImageIds.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mMyContext);
			// Setting the picture to imageView
			i.setImageResource(mMyImageIds[position]);
			// To set the picture wide high
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			// Reset Layout wide high
			i.setLayoutParams(new Gallery.LayoutParams(140, 200));
			// set Gallery background
			i.setBackgroundResource(mMyGalleryItemBackground);
			// return imageView object
			return i;
		}
	}
}