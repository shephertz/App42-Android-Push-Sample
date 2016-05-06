/**
 * -----------------------------------------------------------------------
 *     Copyright  2010 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.plugin;

import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * The Class RichWebView.
 *
 * @author Vishnu Garg
 */
public class RichWebView {
	
	/** The m custom view. */
	private View mCustomView = null;
	
	/** The m web chrome client. */
	private MyWebChromeClient mWebChromeClient = null;
	
	/** The web view. */
	private WebView webView;
	
	/** The frame layout. */
	private FrameLayout frameLayout;
	
	/** The progress bar. */
	private ProgressBar progressBar;

	/**
	 * Used to build WebView and load Html and image content over it.
	 *
	 * @param webFrame the web frame
	 * @param webView the web view
	 * @param progress the progress
	 * @param isUrlContent the is url content
	 * @param content the content
	 */
	public RichWebView(FrameLayout webFrame, WebView webView,
			ProgressBar progress,boolean isUrlContent,String content) {
		this.webView = webView;
		this.frameLayout = webFrame;
		this.progressBar = progress;
		webView.setVisibility(View.VISIBLE);
		WebSettings webSettings = webView.getSettings();
		webSettings.setAllowFileAccess(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
		webView.setWebViewClient(new MyWebClient());
		mWebChromeClient = new MyWebChromeClient();
		webView.setWebChromeClient(mWebChromeClient);
	//	webView.setBackgroundColor(R.color.transparent);
		if(isUrlContent)
			webView.loadUrl(content);
		else
			webView.loadData(content, "text/html", "UTF-8");
	}

	/**
	 * Set WebView Client on Android WebView.
	 */
	public class MyWebClient extends WebViewClient {
		
		/* (non-Javadoc)
		 * @see android.webkit.WebViewClient#onPageStarted(android.webkit.WebView, java.lang.String, android.graphics.Bitmap)
		 */
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
		}

		/* (non-Javadoc)
		 * @see android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit.WebView, java.lang.String)
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		/* (non-Javadoc)
		 * @see android.webkit.WebViewClient#onPageFinished(android.webkit.WebView, java.lang.String)
		 */
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			progressBar.setVisibility(View.GONE);
		}
	}

	/**
	 * WebView CromeClient.
	 */
	private class MyWebChromeClient extends WebChromeClient {
		
		/** The m custom view callback. */
		private WebChromeClient.CustomViewCallback mCustomViewCallback;

		/* (non-Javadoc)
		 * @see android.webkit.WebChromeClient#onShowCustomView(android.view.View, android.webkit.WebChromeClient.CustomViewCallback)
		 */
		@Override
		public void onShowCustomView(View view,
				WebChromeClient.CustomViewCallback callback) {
			webView.setVisibility(View.GONE);
			if (mCustomView != null) {
				callback.onCustomViewHidden();
				return;
			}
			frameLayout.addView(view);
			mCustomView = view;
			mCustomViewCallback = callback;
			frameLayout.setVisibility(View.VISIBLE);
		}

		/* (non-Javadoc)
		 * @see android.webkit.WebChromeClient#onHideCustomView()
		 */
		@Override
		public void onHideCustomView() {
			if (mCustomView == null)
				return;
			mCustomView.setVisibility(View.GONE);
			frameLayout.removeView(mCustomView);
			mCustomView = null;
			mCustomViewCallback.onCustomViewHidden();
			webView.setVisibility(View.VISIBLE);
		}

		/* (non-Javadoc)
		 * @see android.webkit.WebChromeClient#onCloseWindow(android.webkit.WebView)
		 */
		@Override
		public void onCloseWindow(WebView window) {
			mWebChromeClient.onHideCustomView();
		}
	}
	
	/**
	 * hide customWebView.
	 */
	public void hideCustomView() {
		if(mCustomView!=null)
		mWebChromeClient.onHideCustomView();
	}
	
}
