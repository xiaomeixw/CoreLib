/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.test;

import android.os.Bundle;
import android.widget.Toast;

import com.lee.sdk.app.WebBrowserActivity;
import com.lee.sdk.utils.APIUtils;
import com.lee.sdk.widget.BdActionBar;
import com.lee.sdk.widget.menu.MenuItem;

/**
 * 
 * @author lihong06
 * @since 2014-9-14
 */
public class ActionBarActivity extends WebBrowserActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APIUtils.hasHoneycomb()) {
            android.app.ActionBar actionBar = getActionBar();
            if (null != actionBar) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(this.getTitle());
            }
        }

        setActionBarTitle((String) getTitle());
    }
    
    @Override
    protected String getUrl() {
        return "http://mo.baidu.com";
    }

    @Override
    protected void onCreateOptionsMenuItems(BdActionBar actionBar) {
        actionBar
            .add(0, "分享")
            .add(1, "关闭")
            .add(2, "刷新", getResources().getDrawable(R.drawable.action_bar_menu_reflush));
    }

    @Override
    protected void onOptionsMenuItemSelected(MenuItem item) {
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
