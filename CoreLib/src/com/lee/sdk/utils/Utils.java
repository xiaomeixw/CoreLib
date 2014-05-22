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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * 
 * @author lihong06
 * @since 2013-7-9
 */
public class Utils {
    
    /**
     * 为程序创建桌面快捷方式。
     * 
     * @param activity 指定当前的Activity为快捷方式启动的对象
     * @param nameId 快捷方式的名称
     * @param iconId 快捷方式的图标
     * @param appendFlags 需要在快捷方式启动应用的Intent中附加的Flag
     */
    public static void addShortcut(Activity activity, int nameId, int iconId, int appendFlags) {
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

        // 快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, activity.getString(nameId));
        shortcut.putExtra("duplicate", false); // 不允许重复创建

        // 指定当前的Activity为快捷方式启动的对象
        ComponentName comp = new ComponentName(activity.getPackageName(), activity.getClass().getName());
        Intent intent = new Intent(Intent.ACTION_MAIN).setComponent(comp);
        if (appendFlags != 0) {
            intent.addFlags(appendFlags);
        }
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

        // 快捷方式的图标
        ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(activity, iconId);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

        activity.sendBroadcast(shortcut);
    }
    
    /**
     * 通过应用名称和包体名称判断桌面是否已经有对应的快捷方式
     * 
     * @param context {@link Context}
     * @param shortcutName 应用名称
     * @param packageName 包体名称
     * 
     * @return 是否已有快捷方式，在系统DB读取中出现异常时，默认返回true
     */
    public static boolean hasShortcut(Context context, String shortcutName, String packageName) {
        boolean res = true;
        if (context != null 
                && !TextUtils.isEmpty(shortcutName) 
                && !TextUtils.isEmpty(packageName)) {
            Uri uri = getShortcutUri();
            res = hasShortcut(context, shortcutName, packageName, uri);
        }
        return res;
    }
    
    /**
     * 通过应用名称和包体名称判断桌面是否已经有对应的快捷方式
     * 
     * @param context {@link Context}
     * @param shortcutName 应用名称
     * @param packageName 包体名称
     * @param uri {@link Uri}
     * 
     * @return 是否已有快捷方式，在系统DB读取中出现异常时，默认返回true
     */
    private static boolean hasShortcut(Context context, String shortcutName, String packageName, Uri uri) {
        if (context == null 
                || TextUtils.isEmpty(shortcutName) 
                || TextUtils.isEmpty(packageName) 
                || uri == null) {
            return true;
        }
        
        boolean res = false;
        
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    uri, new String[] { "title", "intent" }, "title=?", 
                    new String[] { shortcutName }, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("intent");
                if (index >= 0 && index < cursor.getColumnCount()) {
                    do {
                        String intentString = cursor.getString(index);
                        if (intentString != null && intentString.contains(packageName)) {
                            res = true;
                            break;
                        }
                    } while(cursor.moveToNext());
                }
            }
            
        } catch (Exception e) {
            res = true;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        
        return res;
    }

    /**
     * 得到快捷方式的URI
     * 
     * @return URI
     */
    private static Uri getShortcutUri() {
        String authority = "com.android.launcher.settings";
        if (APIUtils.hasFroyo()) {
            authority = "com.android.launcher2.settings";
        }

        final Uri contentUri = Uri.parse("content://" + authority + "/favorites?notify=true");
        return contentUri;
    }
    
    /**
     * Hides the input method.
     * 
     * @param context context
     * @param view The currently focused view
     * @return success or not.
     */
    public static boolean hideInputMethod(Context context, View view) {
        if (context == null || view == null) {
            return false;
        }

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        return false;
    }

    /**
     * Show the input method.
     * 
     * @param context context
     * @param view The currently focused view, which would like to receive soft keyboard input
     * @return success or not.
     */
    public static boolean showInputMethod(Context context, View view) {
        if (context == null || view == null) {
            return false;
        }

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.showSoftInput(view, 0);
        }

        return false;
    }

    public static float pixelToDp(Context context, float val) {
        float density = context.getResources().getDisplayMetrics().density;
        return val * density;
    }
    
    public static String getHashedFileName(String url) {   
        if (url == null || url.endsWith("/" )) {
            return null ;
        }

        String suffix = getSuffix(url);
        StringBuilder sb = null;
        
        try {
            MessageDigest digest = MessageDigest. getInstance("MD5");
            byte[] dstbytes = digest.digest(url.getBytes("UTF-8")); // GMaFroid uses UTF-16LE
            sb = new StringBuilder();
            for (int i = 0; i < dstbytes.length; i++) {
                sb.append(Integer. toHexString(dstbytes[i] & 0xff));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (null != sb && null != suffix) {
            return sb.toString() + "." + suffix;
        }
       
        return null;
    }
    
    private static String getSuffix(String fileName) {
        int dot_point = fileName.lastIndexOf( ".");
        int sl_point = fileName.lastIndexOf( "/");
        if (dot_point < sl_point) {
            return "" ;
        }
        
        if (dot_point != -1) {
            return fileName.substring(dot_point + 1);
        }
        
        return null;
    }
    
    /**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     *
     * @param context The application's environment.
     * @param intent The Intent action to check for availability.
     *
     * @return True if an Intent with the specified action can be sent and
     *         responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();

        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        return list.size() > 0;
    }
    
    /**
     * DisplayMetrics 对象
     */
    private static DisplayMetrics sDisplayMetrics;
    
    /**
     * 得到显示宽度
     * 
     * @param context Context
     * 
     * @return 宽度
     */
    public static int getDisplayWidth(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.widthPixels;
    }
    
    /**
     * 得到显示高度
     * 
     * @param context Context
     * 
     * @return 高度
     */
    public static int getDisplayHeight(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.heightPixels;
    }
    
    /**
     * 得到显示密度
     * 
     * @param context Context
     * 
     * @return 密度
     */
    public static float getDensity(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.density;
    }
    
    /**
     * 得到DPI
     * 
     * @param context Context
     * 
     * @return DPI
     */
    public static int getDensityDpi(Context context) {
        initDisplayMetrics(context);
        return sDisplayMetrics.densityDpi;
    }
    
    /**
     * 初始化DisplayMetrics
     * 
     * @param context Context
     */
    private static void initDisplayMetrics(Context context) {
        if (null == sDisplayMetrics) {
            if (null != context) {
                sDisplayMetrics = context.getResources().getDisplayMetrics();
            }
        }
    }
    
    /**
     * 判断一个字符串是否为合法url
     * 
     * @param query String
     * @return true: 是合法url
     */
    public static boolean isUrl(String query) {
        Matcher matcher = Patterns.WEB_URL.matcher(query);
        if (matcher.matches()) {
            return true;
        } 

        return false;
    }
    
    /**
     * 网络是否可用。(
     * 
     * @param context
     *            context
     * @return 连接并可用返回 true
     */
    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        // return networkInfo != null && networkInfo.isConnected();
        boolean flag = networkInfo != null && networkInfo.isAvailable();
        return flag;
    }
    
    /**
     * 获取活动的连接。
     * 
     * @param context
     *            context
     * @return 当前连接
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return null;
        }
        return connectivity.getActiveNetworkInfo();
    }
    
    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
