package com.lee.sdk.test;

import com.lee.sdk.test.widget.ImageGridView;

import android.os.Bundle;

public class CacheTestActivity extends GABaseActivity {
    private ImageGridView mGridView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGridView = new ImageGridView(this, false);
        setContentView(mGridView);
        
        mGridView.loadImageAsync();
    }
}
