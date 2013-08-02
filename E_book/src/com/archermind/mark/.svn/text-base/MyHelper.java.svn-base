/**
 * @Project:Archermindreader
 * @ClassName:MyHelper
 * @Version 1.0
 * @Author xuegang.fu
 * @Update minmin.guo
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.mark;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyHelper extends SQLiteOpenHelper {

	public MyHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	/**
	 * create one database
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	/**
	 * update the database
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS tag");
		onCreate(db);
	}

}
