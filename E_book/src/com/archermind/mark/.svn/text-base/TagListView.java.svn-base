/**
 * @Project:Archermindreader
 * @ClassName:TagListView
 * @Version 1.0
 * @Author xuegang.fu
 * @Update minmin.guo
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.mark;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.archermind.main.R;

public class TagListView extends LinearLayout {
	private TextView mText;
	private ImageView mIcon;
/**
 * show the tags with the name and path
 * @param context
 * @param tagMess
 */
	public TagListView(Context context, ReaderTags tagMess) {
		super(context);
		this.setOrientation(HORIZONTAL);
		mIcon = new ImageView(context);
		mIcon.setImageResource(R.drawable.favourite);

		mIcon.setPadding(10, 5, 10, 5);
		addView(mIcon, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		mText = new TextView(context);
		mText.setText(tagMess.title + "\n" + tagMess.filename);
		mText.setTextSize(10);
		addView(mText, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
	}

	public void setText(String name) {
		mText.setText(name);
	}

	public void setIcon(int icon) {
		mIcon.setImageResource(icon);
	}
}
