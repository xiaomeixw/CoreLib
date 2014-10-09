package com.lee.sdk.test.viewpager;

import android.content.Intent;

import com.lee.sdk.test.BaseListActivity;

/**
 * 
 * @author lihong06
 * @since 2014-2-21
 */
public class ViewPagerActivity extends BaseListActivity {
    @Override
    public Intent getQueryIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.lee.sdk.test.intent.category.VIEWPAGER");

        return intent;
    }
}
