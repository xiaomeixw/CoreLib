package com.lee.sdk.test;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.lee.sdk.app.ActionBarBaseActivity;
import com.lee.sdk.widget.BdActionBar;
import com.lee.sdk.widget.menu.MenuItem;

public class WebViewActivity extends ActionBarBaseActivity {

    WebView mWebView;
    String mUrl = "http://bitkiller.duapp.com/jsobj.html";
    String mUrl2 = "file:///android_asset/html/test.html";
    String mUrl3 = "file:///android_asset/html/js3.html";
    Object mJsObj = new JSInterface();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JSInterface(), "jsInterface");
        
        ActionBar actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.hide();
        }
        
        setActionBarTitle((String) getTitle());
    }

    @Override
    protected void onCreateOptionsMenuItems(BdActionBar actionBar) {
        actionBar.add(1, "加载").add(2, "刷新");
    }

    @Override
    protected void onOptionsMenuItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case 1:
            mWebView.loadUrl(mUrl2);
            break;

        case 2:
            mWebView.reload();
            break;
        }
    }

    class JSInterface {
        @JavascriptInterface
        public String onButtonClick(String text) {
            final String str = text;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("leehong2", "onButtonClick: text = " + str);
                    Toast.makeText(getApplicationContext(), "onButtonClick: text = " + str, Toast.LENGTH_LONG).show();
                }
            });

            return "This text is returned from Java layer.  js text = " + text;
        }

        @JavascriptInterface
        public void onImageClick(String url, int width, int height) {
            final String str = "onImageClick: text = " + url + "  width = " + width + "  height = " + height;
            Log.i("leehong2", str);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
