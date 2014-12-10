/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.test.gif;

import java.io.File;
import java.io.FileFilter;

import android.os.Bundle;

import com.lee.sdk.test.BaseFragmentActivity;
import com.lee.sdk.test.widget.ImageGridView;

/**
 * 
 * @author lihong06
 * @since 2014-10-12
 */
public class GifLocalActivity extends BaseFragmentActivity {
    private ImageGridView mGridView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGridView = new ImageGridView(this, false);
        setContentView(mGridView);
        
        mGridView.loadImageAsync(new FileFilter() {
            
            @Override
            public boolean accept(File pathname) {
                if (pathname.getAbsolutePath().endsWith(".gif")) {
                    return true;
                }
                return false;
            }
        });
    }
}
