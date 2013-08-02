/**
 * @Project:Archermindreader
 * @ClassName:ListAllFileActivity
 * @Version 1.0
 * @Author minmin.guo
 * @Update shaojian.ni
 * @Date:2012.4.19
 * Copyright (C) 2012 The Android Open Source Project. 
 */
package com.archermind.load;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.archermind.main.ArchermindReaderActivity;
import com.archermind.main.MainActivity;
import com.archermind.main.R;
import com.archermind.scrollreader.ReadBookActivity;

public class ListAllFileActivity extends ListActivity {
	private ArrayList<File> mFileNameLists = new ArrayList<File>();
	private Bundle mBundle;
	private String mFileNameKey = "fileName";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelist);

		new AsyncTask<Integer, Integer, String[]>() {

			private ProgressDialog mDialog;

			protected void onPreExecute() {
				mDialog = ProgressDialog.show(ListAllFileActivity.this, "",
						getString(R.string.finding));
				super.onPreExecute();
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				Intent intent = new Intent();
				intent.setClass(ListAllFileActivity.this, MainActivity.class);
				ListAllFileActivity.this.startActivity(intent);
			}

			protected String[] doInBackground(Integer... params) {
				if (!android.os.Environment.getExternalStorageState().equals(
						android.os.Environment.MEDIA_MOUNTED)) {

				} else {
					GetFiles(new File("/sdcard"));

				}
				return null;
			}

			protected void onPostExecute(String[] result) {
				mDialog.dismiss();
				Toast.makeText(ListAllFileActivity.this,
						getString(R.string.finded), Toast.LENGTH_LONG).show();
				init();

				super.onPostExecute(result);
			}
		}.execute(0);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		if (android.os.Environment.getExternalStorageState().equals(

		android.os.Environment.MEDIA_MOUNTED)) {

			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) l
					.getItemAtPosition(position);
			final String bookname = (String) map.get("Bookpath");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.pleaseselect));
			builder.setPositiveButton(getString(R.string.pagestyle),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							mBundle = new Bundle();
							Intent intent = new Intent(
									ListAllFileActivity.this,
									ArchermindReaderActivity.class);
							mBundle.putString(mFileNameKey, bookname);
							intent.putExtras(mBundle);
							startActivityForResult(intent, 0);
						}
					});

			builder.setNegativeButton(getString(R.string.scrollstyle),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							mBundle = new Bundle();
							Intent intent = new Intent(
									ListAllFileActivity.this,
									ReadBookActivity.class);
							mBundle.putString(mFileNameKey, bookname);
							intent.putExtras(mBundle);
							startActivityForResult(intent, 0);
						}
					});
			builder.show();
		} else {
			Toast.makeText(this, getString(R.string.nosdcard),
					Toast.LENGTH_LONG).show();
		}

	}

	public void GetFiles(File filePath) {

		File[] mFiles = filePath.listFiles();
		// mFileNameLists=new ArrayList<File>();
		if (mFiles != null) {
			for (File f : mFiles) {
				if (f.isDirectory()) {
					GetFiles(f);
				} else {
					if (f.getName().toLowerCase().endsWith(".txt")) {
						this.mFileNameLists.add(f);
					}
				}
			}
		}
	}

	private ArrayList<Map<String, Object>> getMapData(ArrayList<File> list) {
		ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		HashMap<String, Object> item;
		int i = 0;
		for (i = 0; i < list.size(); i++) {
			item = new HashMap<String, Object>();
			String path = list.get(i).getPath();
			String name = list.get(i).getName();
			item.put("Bookname", name);
			item.put("Bookpath", path);
			data.add(item);

		}
		return data;
	}

	public void init() {
		final SimpleAdapter adapter = new SimpleAdapter(this,
				getMapData(mFileNameLists), R.layout.relative, new String[] {
						"Bookname", "Bookpath" }, new int[] { R.id.Bookname,
						R.id.Bookpath });
		setListAdapter(adapter);
		EditText edit = (EditText) findViewById(R.id.edit);

		edit.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View arg0, int keyCode, KeyEvent KeyEvent) {
				if (keyCode == android.view.KeyEvent.KEYCODE_DEL) {
					EditText edit = (EditText) arg0;
					adapter.getFilter().filter(edit.getText().toString());
					adapter.notifyDataSetChanged();
				}
				return false;
			}

		});

		edit.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				String str = s.toString();
				adapter.getFilter().filter(str);
				adapter.notifyDataSetChanged();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
	}

}