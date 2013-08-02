package com.archermind.main;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

public class Splashing extends Activity {
	public static ArrayList<File> mFileNameLists = new ArrayList<File>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 全屏
		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setContentView(R.layout.splash);
		new AsyncTask<Integer, Integer, String[]>() {

			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				Splashing.this.finish();
			}

			protected String[] doInBackground(Integer... params) {
				if (!android.os.Environment.getExternalStorageState().equals(
						android.os.Environment.MEDIA_MOUNTED)) {

				} else {
					GetFiles(new File("/sdcard"));
					Intent intent = new Intent();
					intent.setClass(Splashing.this, MainActivity.class);
					startActivity(intent);
					Splashing.this.finish();
				}
				return null;
			}

			protected void onPostExecute(String[] result) {
				// Toast.makeText(Splashing.this, getString(R.string.finded),
				// Toast.LENGTH_LONG).show();
				super.onPostExecute(result);
			}
		}.execute(0);

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
}
