package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private String url = "https://httpbin.org";

    private WebView mWebView;
    private ProgressDialog progressDialog;
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;
    private boolean showProgressDialog = true;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_web_view);

        mWebView = (WebView) findViewById(R.id.activity_webview);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("loading");
        progressDialog.setCancelable(false);

        WebSettings mWebViewSettings = mWebView.getSettings();
        mWebViewSettings.setJavaScriptEnabled(true);
        mWebViewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                loadErrorPage();
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progressDialog != null && !MainActivity.this.isFinishing() && showProgressDialog) {
                    progressDialog.show();
                    showProgressDialog = false; // Prevent the dialog from showing again for this load
                }

                if (progress == 100 && progressDialog != null) {
                    progressDialog.dismiss();
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    showProgressDialog = true; // Reset the flag when page loading completes
                }
            }
        });

        if (isNetworkAvailable()) {
            mWebView.loadUrl(url);
        } else {
            loadErrorPage();
        }

        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "eroor load", Toast.LENGTH_SHORT).show();
                    loadErrorPage();
                }
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable, 15000);
    }

    private void loadErrorPage() {
        String errorPage ="eerorrr" ;
        mWebView.loadData(errorPage, "text/html", "UTF-8");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onDestroy() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.setWebChromeClient(null);
            mWebView.setWebViewClient(null);
            mWebView.destroy();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }
}
