/**
 * @Project:Archermindreader
 * @ClassName:MTagAdapter
 * @Version 1.0
 * @Author xuegang.fu
 * @Update minmin.guo
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.mark;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MTagAdapter extends BaseAdapter {
	private Context mContext;
	private List<ReaderTags> mItems;

	public MTagAdapter(Context context, List<ReaderTags> mItems) {
		this.mContext = context;
		this.mItems = mItems;
	}

	public int getCount() {
		return mItems.size();
	}

	public Object getItem(int position) {
		return mItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * show the list with the title and the filename
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		TagListView btv;
		if (convertView == null) {
			btv = new TagListView(mContext, mItems.get(position));
		} else {
			btv = (TagListView) convertView;
			btv.setText(mItems.get(position).title + "\n"
					+ mItems.get(position).filename);
		}
		return btv;
	}
}