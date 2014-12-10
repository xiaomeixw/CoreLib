/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.test.gif;

import java.util.ArrayList;

import android.os.Bundle;

import com.lee.sdk.test.BaseFragmentActivity;
import com.lee.sdk.test.widget.ImageGridView;

/**
 * 
 * @author lihong06
 * @since 2014-10-12
 */
public class GifNetworkActivity extends BaseFragmentActivity {
    private ImageGridView mGridView;
    private ArrayList<String> mDatas = new ArrayList<String>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGridView = new ImageGridView(this);
        setContentView(mGridView);
        
        mGridView.postDelayed(new Runnable() {
            @Override
            public void run() {
                genDatas();
                mGridView.setData(mDatas);
            }
        }, 0);
//        mGridView.loadImageAsync();
//        mGridView.setOnCovertDataListener(new OnCovertDataListener() {
//            @Override
//            public List<String> converData(List<MediaInfo> datas) {
//                return mDatas;
//            }
//        });
    }
    
    private void genDatas() {
        String[] urls = {
            "http://img2.cache.netease.com/photo/0031/2014-10-09/A85222JM43UD0031.gif",
            "http://img3.cache.netease.com/photo/0031/2014-10-09/900x600_A85223JN43UD0031.gif",
            "http://img3.cache.netease.com/photo/0031/2014-10-09/900x600_A852266P43UD0031.gif",
            "http://img3.cache.netease.com/photo/0031/2014-10-09/A840FJDO43UD0031.gif",
            "http://img2.cache.netease.com/photo/0031/2014-10-09/900x600_A840FK8F43UD0031.gif",
            "http://cdn.duitang.com/uploads/blog/201306/15/20130615213147_Rih2c.thumb.600_0.gif",
            "http://img3.3lian.com/2006/013/08/20051103121420947.gif",
            "http://img4.3lian.com/sucai/img1/23/24.gif",
            "http://pic18.nipic.com/20120104/9183330_165337366000_2.gif",
            "http://cdn.duitang.com/uploads/item/201305/10/20130510232517_5uH2E.thumb.600_0.gif",
            "http://photo.l99.com/bigger/13/1336832115572_bq422e.gif",
            "http://pic.962.net/up/2013-2/2013022510023730088.gif",
            "http://img2.duitang.com/uploads/item/201303/06/20130306193826_RzdF5.gif",
            "http://img4.duitang.com/uploads/item/201209/13/20120913200115_EMrSN.thumb.700_0.gif",
            "http://bbs.replays.net/upload/2010/06/05/747/173947887522702.gif",
            "http://i165.photobucket.com/albums/u53/Ruknix/AnimationWizard2-1.gif",
            "http://i2.hoopchina.com.cn/blogfile/201206/12/133950761138387.gif",
            "http://static14.photo.sina.com.cn/middle/796e19fbtvb1a6ze6ylcd&690",
            "http://s1.dwstatic.com/group1/M00/EA/45/9812a549e5846fee94a1f9a516db0f08.gif",
            "http://www.ql2007.com/img/aHR0cDovL3NvZnQubXVtYXlpLm5ldC91cGltYWdlcy8yMDA2LTUvMjAwNjUxNzM2OTU4MjM3LmdpZg==.jpg",
            "http://ww1.sinaimg.cn/bmiddle/599ad67agw1du8ibyojffg.gif",
            "http://www.daomei.net.cn/uploads/allimg/130226/1-130226150257.gif",
            "http://ww1.sinaimg.cn/bmiddle/6764743djw1dj7q0h9hmng.gif",
            "http://www.digitalbusstop.com/wp-content/uploads/2010/09/Hypnotic-Coil.gif",
        };

        for (String url : urls) {
            mDatas.add(url);
        }
    }
}
