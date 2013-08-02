/**
 * @Project:Archermindreader
 * @ClassName:ReaderDataBase
 * @Version 1.0
 * @Author xuegang.fu
 * @Update minmin.guo
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.mark;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ReaderDataBase {
	static final String DB_NAME = "reader.db";
	static final String TB_NAME = "tag";
	public static float sResult;

	static void createReaderDataBase(Context mContext) {
		SQLiteDatabase db = mContext.openOrCreateDatabase(DB_NAME,
				Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TB_NAME
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,title VARCHAR,filename VARCHAR,po_float FLOAT);");
		db.close();
	}

	/**
	 * Get the data from database
	 * 
	 * @param mContext
	 * @return
	 */
	public static List<ReaderTags> getReaderDataBase(Context mContext) {
		SQLiteDatabase db = mContext.openOrCreateDatabase(DB_NAME,
				Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TB_NAME
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,title VARCHAR,filename VARCHAR,po_float FLOAT);");
		Cursor cur = db.query(TB_NAME, new String[] { "id", "title",
				"filename", "po_float" }, null, null, null, null, null);
		List<ReaderTags> list = new ArrayList<ReaderTags>();
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			ReaderTags tag = new ReaderTags();
			tag._id = cur.getInt(0);
			tag.title = cur.getString(1);
			tag.filename = cur.getString(2);
			tag.position = cur.getFloat(3);
			list.add(tag);
			cur.moveToNext();
		}
		cur.close();
		db.close();
		return list;
	}

	/**
	 * find item where title is "LastReadProcess",then return the process of
	 * last reading
	 * 
	 * @param mContext
	 * @param file
	 * @return
	 */
	public static float search(Context mContext, String title, String file) {
		MyHelper helper = new MyHelper(mContext, DB_NAME, null, 1);
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TB_NAME
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,title VARCHAR,filename VARCHAR,po_float FLOAT);");
		String sql = "Select po_float from tag where title = '" + title
				+ "' and filename = '" + file + "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur.moveToFirst() == true) {
			cur.moveToFirst();
			do {
				sResult = ((cur.getFloat(cur.getColumnIndex("po_float"))) * 100);
			} while (cur.moveToNext());
			cur.close();
			db.close();
			return sResult;
		} else {
			cur.close();
			db.close();
			return 0;
		}

	}

	/**
	 * insert one tag into the database
	 * 
	 * @param mContext
	 * @param title
	 * @param file
	 * @param pos
	 */
	public static void insertDataBase(Context mContext, String title,
			String file, float pos) {
		MyHelper helper = new MyHelper(mContext, DB_NAME, null, 1);
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TB_NAME
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,title VARCHAR,filename VARCHAR,po_float FLOAT);");
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("filename", file);
		values.put("po_float", pos);
		db.insert(TB_NAME, "id", values);
		db.close();
		values.clear();
	}

	/**
	 * delete one tag from the database
	 * 
	 * @param mContext
	 * @param id
	 */
	public static void deleteOne(Context mContext, int id) {
		MyHelper helper = new MyHelper(mContext, DB_NAME, null, 1);
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TB_NAME
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,title VARCHAR,filename VARCHAR,po_float FLOAT);");
		String where = "id=?";
		String[] whereValue = { Integer.toString(id) };
		db.delete(TB_NAME, where, whereValue);
		db.close();
	}

	/**
	 * delete all of the tags from the database
	 * 
	 * @param mContext
	 */
	public static void deleteAll(Context mContext) {
		MyHelper helper = new MyHelper(mContext, DB_NAME, null, 1);
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TB_NAME
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,title VARCHAR,filename VARCHAR,po_float FLOAT);");
		db.delete(TB_NAME, null, null);
		db.close();
	}

	/**
	 * update the tags for last process of reading
	 * 
	 * @param mContext
	 * @param title
	 * @param file
	 * @param position
	 */
	public static void upData(Context mContext, String title, String file,
			float position) {
		MyHelper helper = new MyHelper(mContext, DB_NAME, null, 1);
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("filename", file);
		values.put("po_float", position);
		db.update(TB_NAME, values, "title ='LastReadProcess' " + "and "
				+ "filename ='" + file + "';", null);
		db.close();
		values.clear();
	}
}