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

package com.lee.sdk.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.lee.sdk.Configuration;

/**
 * NetUtils
 * 
 * @author LiuXinjian
 * @since 2014-1-3
 */
public final class NetUtils {

    /** Log TAG */
    private static final String TAG = "NetUtils";

    /** Log switch */
    private static final boolean DEBUG = Configuration.DEBUG & true;

    /**
     * Private constructor to prohibit nonsense instance creation.
     */
    private NetUtils() {
    }

    /**
     * 网络是否可用。(
     * 
     * @param context context
     * @return 连接并可用返回 true
     */
    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        // return networkInfo != null && networkInfo.isConnected();
        boolean flag = networkInfo != null && networkInfo.isAvailable();
        if (DEBUG) {
            Log.d(TAG, "isNetworkConnected, rtn: " + flag);
        }
        return flag;
    }

    /**
     * wifi网络是否可用
     * 
     * @param context context
     * @return wifi连接并可用返回 true
     */
    public static boolean isWifiNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        // return networkInfo != null && networkInfo.isConnected();
        boolean flag = networkInfo != null && networkInfo.isAvailable()
                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        if (DEBUG) {
            Log.d(TAG, "isWifiNetworkConnected, rtn: " + flag);
        }
        return flag;

    }

    /**
     * 获取活动的连接。
     * 
     * @param context context
     * @return 当前连接
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return null;
        }
        return connectivity.getActiveNetworkInfo();
    }

    /**
     * 从网上获取内容get方式
     * 
     * @param url url
     * @return string
     * @throws IOException IOException
     * @throws ClientProtocolException ClientProtocolException
     */
    public static String getStringFromUrl(String url) throws ClientProtocolException, IOException {
        HttpGet get;
        try {
            get = new HttpGet(url);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(get);
        if (response != null) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity, "UTF-8");
            }
        }
        return null;
    }

    /**
     * 根据server下发的Content-Encoding，获取适当的inputstream.当content-encoding为gzip时，返回GzipInputStream
     * 否则返回原有的inputStream
     * 
     * @param resEntity {@link HttpEntity}
     * @return InputStream or null
     * @throws IOException {@link IOException}
     */
    public static InputStream getSuitableInputStream(HttpEntity resEntity) throws IOException {
        if (resEntity == null) {
            return null;
        }
        InputStream inputStream = resEntity.getContent();
        if (inputStream != null) {
            Header header = resEntity.getContentEncoding();
            if (header != null) {
                String contentEncoding = header.getValue();
                if (!TextUtils.isEmpty(contentEncoding) && contentEncoding.toLowerCase().indexOf("gzip") > -1) {
                    inputStream = new GZIPInputStream(inputStream);
                }
            }
        }
        return inputStream;
    }
}
