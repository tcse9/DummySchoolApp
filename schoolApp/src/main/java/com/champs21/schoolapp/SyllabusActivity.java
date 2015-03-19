package com.champs21.schoolapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;

import com.champs21.freeversion.HomeBaseActivity;

public class SyllabusActivity extends HomeBaseActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_syllabus);
		
		WebView syllabus;
		syllabus=(WebView)findViewById(R.id.webview);
		syllabus.getSettings();
		syllabus.setBackgroundColor(0x00000000);
		
		syllabus.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		syllabus.getSettings().setBuiltInZoomControls(true);
		//syllabus.loadData(getIntent().getStringExtra("html_data"), "text/html", "utf-8");
		showWebViewContent(getIntent().getStringExtra("html_data"), syllabus);
		
	}

	
	
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	private void showWebViewContent(String text, WebView webView) {
		
		final String mimeType = "text/html";
		final String encoding = "UTF-8";

		webView.loadDataWithBaseURL("", text, mimeType, encoding, null);
		WebSettings webViewSettings = webView.getSettings();
		webViewSettings.setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient());

				
	}

}
