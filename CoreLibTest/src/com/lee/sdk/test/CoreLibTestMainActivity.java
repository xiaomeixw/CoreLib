package com.lee.sdk.test;

import java.io.UnsupportedEncodingException;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CoreLibTestMainActivity extends BaseListActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i("DemoShell", "os_name:" + System.getProperty("os.name"));
        Log.i("DemoShell", "os_arch:" + System.getProperty("os.arch"));
        Log.i("DemoShell", "os_version:" + System.getProperty("os.version"));
        
        //testStr();
        
        setUseDefaultPendingTransition(true);
    }
    
    void testStr() {
        byte[] arrays = { -119, -28, -112, -91, -113, -84, -119, 74, -107, -126, -119, 93, -127, 67, -115, -95, -109,
                86, -118, 119, 63, -105, -71, 81, 82, 67, 111, 100, 101, 0 };
        
        String encoding = "GBK";
        String str;
        try {
            str = new String(arrays, encoding);
            Log.d("CoreLibTestMainActivity", "  str = " + str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDestroy() {
//        GoogleAnalyticsBL.getInstance().dispatch();

        super.onDestroy();
    }

    @Override
    public Intent getQueryIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(CORELIT_TEST_CAGEGORY);

        return intent;
    }
    
    @Override
    public void finish() {
        super.finish();
    }
}
