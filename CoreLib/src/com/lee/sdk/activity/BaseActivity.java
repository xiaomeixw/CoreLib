/*
 * Copyright (C) 2013 Lee Hong (http://blog.csdn.net/leehong2005)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lee.sdk.activity;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.lee.sdk.app.BaseApplication;

/**
 * The base activity, this activity is used to record the top activity.
 * 
 * @author Li Hong
 * @since 2011/12/14
 */
public class BaseActivity extends Activity {
    /** 下一次Activity启动时，新Activity的进入动画 */
    private static int sNextEnterAnimWhenStarting;
    /** 下一次Activity启动时，旧Activity的退出动画 */
    private static int sNextExitAnimWhenStarting;
    /** 下一次Activity结束时，新Activity的进入动画 */
    private static int sNextEnterAnimWhenFinishing;
    /** 下一次Activity结束时，旧Activity的退出动画 */
    private static int sNextExitAnimWhenFinishing;
    /** Invalid animation */
    private static final int INVALID_ANIM = 0;
    /** 启动时，新Activity的进入动画 */
    private int mEnterAnimWhenStarting = INVALID_ANIM;
    /** 启动时，旧Activity的退出动画 */
    private int mExitAnimWhenStarting = INVALID_ANIM;
    /** 结束时，新Activity的进入动画 */
    private int mEnterAnimWhenFinishing = INVALID_ANIM;
    /** 结束时，旧Activity的退出动画 */
    private int mExitAnimWhenFinishing = INVALID_ANIM;

    /**
     * 设置下一次Activity的切换动画，enter与exit同时为0时用默认
     * 
     * @param enterAnimWhenStarting 下一次Activity启动时，新Activity的进入动画
     * @param exitAnimWhenStarting 下一次Activity启动时，旧Activity的退出动画
     * @param enterAnimWhenFinishing 下一次Activity结束时，新Activity的进入动画
     * @param exitAnimWhenFinishing 下一次Activity结束时，旧Activity的退出动画
     * @see Activity#overridePendingTransition(int, int)
     */
    public static void setNextPendingTransition(int enterAnimWhenStarting, int exitAnimWhenStarting,
            int enterAnimWhenFinishing, int exitAnimWhenFinishing) {
        BaseActivity.sNextEnterAnimWhenStarting = enterAnimWhenStarting;
        BaseActivity.sNextExitAnimWhenStarting = exitAnimWhenStarting;
        BaseActivity.sNextEnterAnimWhenFinishing = enterAnimWhenFinishing;
        BaseActivity.sNextExitAnimWhenFinishing = exitAnimWhenFinishing;
    }

    /**
     * 设置本次的切换动画，enter与exit同时为0时用默认
     * 
     * @param enterAnimWhenStarting 启动时，新Activity的进入动画
     * @param exitAnimWhenStarting 启动时，旧Activity的退出动画
     * @param enterAnimWhenFinishing 结束时，新Activity的进入动画
     * @param exitAnimWhenFinishing 结束时，旧Activity的退出动画
     * @see Activity#overridePendingTransition(int, int)
     */
    protected void setPendingTransition(int enterAnimWhenStarting, int exitAnimWhenStarting,
            int enterAnimWhenFinishing, int exitAnimWhenFinishing) {
        mEnterAnimWhenStarting = enterAnimWhenStarting;
        mExitAnimWhenStarting = exitAnimWhenStarting;
        mEnterAnimWhenFinishing = enterAnimWhenFinishing;
        mExitAnimWhenFinishing = exitAnimWhenFinishing;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置本次Activity切换动画
        if (sNextEnterAnimWhenStarting != INVALID_ANIM || sNextExitAnimWhenStarting != INVALID_ANIM) {
            mEnterAnimWhenStarting = sNextEnterAnimWhenStarting;
            mExitAnimWhenStarting = sNextExitAnimWhenStarting;
        }

        if (sNextEnterAnimWhenFinishing != INVALID_ANIM || sNextExitAnimWhenFinishing != INVALID_ANIM) {
            mEnterAnimWhenFinishing = sNextEnterAnimWhenFinishing;
            mExitAnimWhenFinishing = sNextExitAnimWhenFinishing;
        }

        setNextPendingTransition(INVALID_ANIM, INVALID_ANIM, INVALID_ANIM, INVALID_ANIM);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // 添加Activity启动的动画
        if (mEnterAnimWhenStarting != INVALID_ANIM || mExitAnimWhenStarting != INVALID_ANIM) {
            overridePendingTransition(mEnterAnimWhenStarting, mExitAnimWhenStarting);
            mEnterAnimWhenStarting = INVALID_ANIM;
            mExitAnimWhenStarting = INVALID_ANIM;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 设置本次Activity切换动画
        if (sNextEnterAnimWhenStarting != INVALID_ANIM || sNextExitAnimWhenStarting != INVALID_ANIM) {
            mEnterAnimWhenStarting = sNextEnterAnimWhenStarting;
            mExitAnimWhenStarting = sNextExitAnimWhenStarting;
        }

        if (sNextEnterAnimWhenFinishing != INVALID_ANIM || sNextExitAnimWhenFinishing != INVALID_ANIM) {
            mEnterAnimWhenFinishing = sNextEnterAnimWhenFinishing;
            mExitAnimWhenFinishing = sNextExitAnimWhenFinishing;
        }

        setNextPendingTransition(INVALID_ANIM, INVALID_ANIM, INVALID_ANIM, INVALID_ANIM);

        // 添加Activity启动的动画
        if (mEnterAnimWhenStarting != INVALID_ANIM || mExitAnimWhenStarting != INVALID_ANIM) {
            overridePendingTransition(mEnterAnimWhenStarting, mExitAnimWhenStarting);
            mEnterAnimWhenStarting = INVALID_ANIM;
            mExitAnimWhenStarting = INVALID_ANIM;
        }
    }

    @Override
    protected void onResume() {
        Application app = this.getApplication();

        if (app instanceof BaseApplication) {
            ((BaseApplication) app).setTopActivity(this);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Application app = this.getApplication();

        if (app instanceof BaseApplication) {
            Activity topActivity = ((BaseApplication) app).getTopActivity();
            if (null != topActivity && topActivity == this) {
                ((BaseApplication) app).setTopActivity(null);
            }
        }

        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        // 添加Activity退出的动画
        if (mEnterAnimWhenFinishing != INVALID_ANIM || mExitAnimWhenFinishing != INVALID_ANIM) {
            overridePendingTransition(mEnterAnimWhenFinishing, mExitAnimWhenFinishing);
            mEnterAnimWhenFinishing = INVALID_ANIM;
            mExitAnimWhenFinishing = INVALID_ANIM;
        }
    }
}
