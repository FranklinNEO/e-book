/**
 * @Project:Archermindreader
 * @ClassName:BackGround
 * @Version 1.0
 * @Author shaojian.ni
 * @Update minmin.guo
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.scrollreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import com.archermind.main.R;

public class BackGround extends Activity {
	private Gallery mMyGallery;
	private ImageAdapter mMyImageAdapter;
	private String mFilenameString;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changebg);
		final Integer[] myImageIds = { R.drawable.bg, R.drawable.bg001,
				R.drawable.bg002, R.drawable.bg003, R.drawable.bg004,
				R.drawable.bg005, R.drawable.bg006, R.drawable.bg007,
				R.drawable.bg008, R.drawable.bg009, R.drawable.bg010,
				R.drawable.bg011, R.drawable.ic_launcher };
		Bundle bundle = this.getIntent().getExtras();
		mFilenameString = bundle.getString("fileName");
		Log.d("filename", mFilenameString);
		mMyImageAdapter = new ImageAdapter(this, myImageIds);
		mMyGallery = (Gallery) findViewById(R.id.myGallery);// The graph showing
															// the way to set
															// gallery
		mMyGallery.setAdapter(mMyImageAdapter);
		mMyGallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				Resources resources = getBaseContext().getResources();
				Drawable d = resources.getDrawable(myImageIds[position]);
				new AlertDialog.Builder(BackGround.this)
						.setTitle(getString(R.string.bgphoto))
						.setMessage(getString(R.string.changequestion1))
						.setPositiveButton(getString(R.string.yes),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										try {
											// ReadBookActivity ara = new
											// ReadBookActivity();
											ReadBookActivity.POS = position;
											Intent intent = new Intent();
											intent.setClass(BackGround.this,
													ReadBookActivity.class);
											Bundle bundle = new Bundle();
											bundle.putString("fileName",
													mFilenameString);
											intent.putExtras(bundle);
											startActivity(intent);
											BackGround.this.finish();
											if (position >= 0) {
												Toast.makeText(
														BackGround.this,
														getString(R.string.bgischange),
														Toast.LENGTH_LONG)
														.show();
											} else {
												Toast.makeText(
														BackGround.this,
														getString(R.string.bgisnotchange),
														Toast.LENGTH_LONG)
														.show();
											}
											;
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								})
						.setNegativeButton(getString(R.string.no),
								new OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										Toast.makeText(BackGround.this,
												getString(R.string.no),
												Toast.LENGTH_SHORT).show();
									}
								}).show().getWindow().setBackgroundDrawable(d);
			}
		});
	}

	public class ImageAdapter extends BaseAdapter {

		int myGalleryItemBackground;
		private Context myContext;
		private Integer[] myImageIds;

		public ImageAdapter(Context c, Integer[] aid) {
			myContext = c;
			myImageIds = aid;
			TypedArray a = obtainStyledAttributes(R.styleable.Gallery);
			myGalleryItemBackground = a.getResourceId(
					R.styleable.Gallery_android_galleryItemBackground, 0);
			a.recycle();
		}

		public int getCount() {
			return myImageIds.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(myContext);
			// Setting the picture to imageView
			i.setImageResource(myImageIds[position]);
			// To set the picture wide high
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			// Reset Layout wide high
			i.setLayoutParams(new Gallery.LayoutParams(140, 200));
			// set Gallery background
			i.setBackgroundResource(myGalleryItemBackground);
			// return imageView object
			return i;
		}
	}
}
