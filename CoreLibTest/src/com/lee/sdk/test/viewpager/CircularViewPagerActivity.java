package com.lee.sdk.test.viewpager;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lee.sdk.test.BaseFragmentActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.widget.viewpager.CircularPagerAdapter;
import com.lee.sdk.widget.viewpager.CircularViewPager;
import com.lee.sdk.widget.viewpager.PointPageIndicator;

/**
 * 
 * @author lihong06
 * @since 2014-2-21
 */
public class CircularViewPagerActivity extends BaseFragmentActivity {
    private ArrayList<String> mDatas = new ArrayList<String>();
    private CircularViewPager mCircularViewPager;
    private PointPageIndicator mPointPageIndicator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager_circular_layout);

        initDatas();
        init();
    }

    private void initDatas() {
        mDatas.add("百度");
        mDatas.add("网易");
        mDatas.add("SINA");
        mDatas.add("腾讯");
        mDatas.add("阿里巴巴");
    }

    private void init() {
        mCircularViewPager = (CircularViewPager) findViewById(R.id.viewpager);
        mPointPageIndicator = (PointPageIndicator) findViewById(R.id.indicator);

        final CircularPagerAdapter adapter = new CircularPagerAdapter() {
            @Override
            public int getRealCount() {
                return mDatas.size();
            }
            
            @Override
            protected View onInstantiateItem(ViewGroup container, int position) {
                TextView textView = new TextView(CircularViewPagerActivity.this);
                textView.setTextSize(30);
                textView.setGravity(Gravity.CENTER);
                return textView;
            }
            
            @Override
            protected void onConfigItem(View convertView, int position) {
                if (convertView instanceof TextView) {
                    String text = mDatas.get(position);
                    TextView textView = (TextView) convertView;
                    textView.setText(text);
                }
            }
        };
        
        int initPos = 1;
        mCircularViewPager.setAdapter(adapter);
        mCircularViewPager.setCurrentItem(adapter.getCount() / 2 + initPos, false);
        mPointPageIndicator.setViewPager(mCircularViewPager, initPos);
        
        mPointPageIndicator.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                String str = mDatas.get(arg0);
                int currentItem = mCircularViewPager.getCurrentItem();
                View view = adapter.getCurrentView(arg0);
                String strInView = "null";
                if (view instanceof TextView) {
                    strInView = ((TextView) view).getText().toString();
                }
                Log.d("ViewPager", "onPageSelected,  position = " + arg0 + ",  data = " + str + ", currentItem = "
                        + currentItem + ", view = " + strInView);
            }
            
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                
            }
            
            @Override
            public void onPageScrollStateChanged(int arg0) {
                
            }
        });
    }
}
