package com.lee.sdk.test.viewpager;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lee.sdk.test.BaseFragmentActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.widget.viewpager.BdPagerTab;
import com.lee.sdk.widget.viewpager.BdPagerTabHost;
import com.lee.sdk.widget.viewpager.PagerAdapterImpl;

/**
 * 
 * @author lihong06
 * @since 2014-2-21
 */
public class NormalViewPagerActivity extends BaseFragmentActivity {
    ArrayList<String> mDatas = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager_layout);

        initDatas();
        initActionBar();
        initTabHost();
    }

    private void initDatas() {
        mDatas.add("百度");
        mDatas.add("网易");
        mDatas.add("SINA");
        mDatas.add("腾讯");
        mDatas.add("阿里巴巴");
    }

    private void initActionBar() {
    }

    private void initTabHost() {
        // 得到BdPagerTabHost对象，可以new创建，也可从XML中加载
        final BdPagerTabHost tabHostView = (BdPagerTabHost) findViewById(R.id.tabhost);
        // 添加tab
        for (String str : mDatas) {
            tabHostView.addTab(new BdPagerTab().setTitle(str));
        }

        tabHostView.selectTab(0); // 默认第一个tab选中
        // tabHostView.setTabTextSize(32); // 设置tab字体大小，这块可以不用设置，到时候统一调成默认的
        // tabHostView.setTabBarBackground(R.drawable.picture_action_bar_bg); // 设置tab bar的背景色，通常不需要
        // tabHostView.setPageIndicatorDrawable(R.drawable.picture_tab_indicator); //
        // 设置tab的indicator，通常需要设置，默认是红色的。
        tabHostView.layoutTabs(); // 布局tab

        PagerAdapterImpl adapter = new PagerAdapterImpl() {
            @Override
            public int getCount() {
                return tabHostView.getTabCount();
            }

            @Override
            protected View onInstantiateItem(ViewGroup container, int position) {
                TextView textView = new TextView(NormalViewPagerActivity.this);
                textView.setTextSize(30);
                textView.setTextColor(Color.RED);
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
        
        // 设置adapter，默认选中第1个tab。
        tabHostView.setPagerAdapter(adapter, 0);
    }
}
