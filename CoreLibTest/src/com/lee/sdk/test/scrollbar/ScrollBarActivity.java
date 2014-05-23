package com.lee.sdk.test.scrollbar;

import android.content.Intent;

import com.lee.sdk.test.BaseListActivity;

public class ScrollBarActivity extends BaseListActivity
{
    @Override
    public Intent getQueryIntent()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory("com.lee.sdk.test.intent.category.SCROLLBAR");
        
        return intent;
    }
}
