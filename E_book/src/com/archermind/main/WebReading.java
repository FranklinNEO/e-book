package com.archermind.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;

public class WebReading extends Activity implements OnClickListener {
	/** Called when the activity is first created。 */
	private WebView webview;
	private String mUriInfo;
	private RelativeLayout rl = null;
	private Button BackBtn = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webreading);
		Bundle bundle = this.getIntent().getExtras();
		mUriInfo = bundle.getString("uriName");
		rl = (RelativeLayout) findViewById(R.id.top);
		BackBtn = (Button) rl.findViewById(R.id.btn_leftTop);
		BackBtn.setOnClickListener(this);
		webview = (WebView) findViewById(R.id.webView1);
		// 设置WebView属性，能够执行JavaScript脚本
		webview.getSettings().setJavaScriptEnabled(true);
		// 加载URL内容
		webview.loadUrl(mUriInfo);
		// 设置web视图客户端
		webview.setWebViewClient(new MyWebViewClient());
	}

	// 设置回退
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// web视图客户端
	public class MyWebViewClient extends WebViewClient {
		public boolean shouldOverviewUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_leftTop:
			// Intent intent = new Intent();
			// intent.setClass(WebReading.this, MainActivity.class);
			WebReading.this.finish();
			// startActivity(intent);
			break;
		default:
			break;
		}
	}
}