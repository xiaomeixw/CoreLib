/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.cache;

import com.lee.sdk.Configuration;


/**
 * Debug flag.
 * 
 * @author Li Hong
 * 
 * @since 2013-7-25
 */
public final class BuildConfig {
    
    /**Debug标志量*/
    public static final boolean DEBUG = Configuration.DEBUG & true;
    
    /**
     * 构造方法
     */
    private BuildConfig() {
        
    }
}
