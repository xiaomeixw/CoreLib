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

package com.lee.sdk.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.lee.sdk.cache.ImageCache.ImageCacheParams;
import com.lee.sdk.cache.ImageResizer.OnProcessBitmapListener;
import com.lee.sdk.cache.ImageWorker.OnLoadImageListener;

/**
 * 这个类封装了加载图片的一些业务逻辑，它先从本地文件查找，如果不行，再从网络请求
 * 
 * @author Li Hong
 * @since 2013-7-23
 */
public final class ImageLoader {
    /**缓存的最大图片数量*/
    /*private*/ static final int MAX_CACHE_NUM = 20;
    /**缓存的大小的百分比*/
    private static final float MAX_CACHE_PERCENT = 0.12f;
    /**加载图片的具体类*/
    private ImageResizer mImageResizer;
    /**当前缓存路径*/
    private String mCacheDir;
    /**Application的Context*/
    private Context mAppContext;
    /**使用Disk缓存*/
    private boolean mUseDiskCache = false;
    /** 缓存最大百分比 */
    private float mMaxCachePercent = MAX_CACHE_PERCENT;
    /** Listener */
    private OnProcessBitmapListener mListener;

    /**
     * 构造方法
     * 
     * @param context context
     */
    private ImageLoader(Context context) {
        mAppContext = context.getApplicationContext();
    }
    
    /**
     * 初始化
     */
    private void init() {
        final Context context = mAppContext; 
        final boolean useDiskCache = mUseDiskCache;
        mImageResizer = new ImageResizer(context);
        
        if (useDiskCache) {
            if (TextUtils.isEmpty(mCacheDir)) {
                throw new IllegalStateException("You must set the disk cache directory if you want to use disk cache");
            }
            
            ImageCacheParams params = new ImageCacheParams(new File(mCacheDir));
            //params.setMemCacheSizeCount(context, MAX_CACHE_NUM);
            params.setMemCacheSizePercent(context, mMaxCachePercent);
            mImageResizer.addImageCache(params);
        } else {
            ImageCacheParams params = new ImageCacheParams(context, "cache_params");
            //params.setMemCacheSizeCount(context, MAX_CACHE_NUM);
            params.setMemCacheSizePercent(context, mMaxCachePercent);
            ImageCache imageCache = new ImageCache(params);
            mImageResizer.setImageCache(imageCache);  
        }
        
        mImageResizer.setImageFadeIn(false);
        mImageResizer.setOnProcessBitmapListener(new OnProcessBitmapListener() {
            @Override
            public Bitmap onProcessBitmap(Object data) {
                Bitmap bitmap = null;
                if (data instanceof ILoadImage) {
                    bitmap = ((ILoadImage) data).loadImage();
                }
                
                if (null == bitmap) {
                    if (null != mListener) {
                        bitmap = mListener.onProcessBitmap(data);
                    }
                }
                
                return bitmap;
            }
        });
    }

    /**
     * 设置图片加载的监听器
     * 
     * @param listener listener
     */
    public void setOnLoadImageListener(OnLoadImageListener listener) {
        mImageResizer.setOnLoadImageListener(listener);
    }
    
    /**
     * Set the listener.
     * 
     * @param listener The OnProcessBitmapListener object.
     */
    public void setOnProcessBitmapListener(OnProcessBitmapListener listener) {
        mListener = listener;
    }
    
    /**
     * 加载图片， 传入的data参数能唯一标识出一个图片文件，我们在内部会根据这个数据通过成一个key，
     * 通过这个key来标识内存缓存中的bimap和磁盘缓存中的文件。
     * <p>
     * 如果你不使用ImageLoader的disk缓存的话，这个data对象可以不用是String类型，而是实现了{@link ILoadImage}接口。
     * <p>
     * 总的说来，传入data要么是String类型的url，要么是{@link ILoadImage}派生类。
     * 
     * @param data 需要加载bitmap的数据, 通常你需要传一个图片的url，这个url能唯一定位这个文件。
     * @param view 需要显示图片的View
     */
    public void loadImage(Object data, IAsyncView view) {
        loadImage(data, view, null);
    }
    
    /**
     * 加载图片
     * 
     * @param data 需要加载bitmap的数据
     * @param view 需要显示图片的View
     * @param listener 图片加载完成的监听器
     */
    public void loadImage(Object data, IAsyncView view, OnLoadImageListener listener) {
        mImageResizer.loadImage(data, view, listener);
    }

    /**
     * 从内存缓存中得到bitmap
     *  
     * @param data 数据
     * @return bitmap对象
     */
    public Bitmap getBitmapFromCache(Object data) {
        return mImageResizer.getBitmapFromCache(data);
    }
    
    /**
     * 清除指定数据的图片
     * 
     * @param data 数据
     */
    public void clearImage(Object data) {
        if (data instanceof String) {
            if (!TextUtils.isEmpty((String) data)) {
                //mImageResizer.clearCache((String) data);
                mImageResizer.clearCacheInternal((String) data);
            }
        }
    }
    
    /**
     * 清除内存中的图片对象
     */
    public void clear() {
        mImageResizer.clearCacheInternal();
    }
    
    /**
     * Get the bitmap count in the memory cache.
     * 
     * @return the size
     */
    public int getBitmapSizeInMemCache() {
        return mImageResizer.getBitmapSizeInMemCache();
    }
    
    /**
     * 暂停当前工作线程
     * 
     * @param pauseWork true表示为暂停，false表示开启
     */
    public void setPauseWork(boolean pauseWork) {
        mImageResizer.setPauseWork(pauseWork);
    }
    
    /**
     * 根据图片路径加载图片
     * 
     * @param filePath 图片路径
     * @return Bitmap对象
     */
    public static Bitmap loadImageFromFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Bitmap bmp = loadBitmapFromFile(file.getAbsolutePath(), 1);
            if (null != bmp) {
                return bmp;
            }
        }
        
        return null;
    }
    
    /***
     * 从文件中加载bitmap
     * 
     * @param name 文件路径
     * @param inSampleSize inSampleSize
     * @return bitmap对象
     */
    public static Bitmap loadBitmapFromFile(String name, int inSampleSize) {
        Bitmap bmp = null;
        try {
            if (!TextUtils.isEmpty(name)) {
                FileInputStream fis = new FileInputStream(new File(name));
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = inSampleSize;
                bmp = BitmapFactory.decodeStream(fis, null, opts);
                fis.close();
                fis = null;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bmp;
    }
    
    /**
     * 创建ImageLoader的Builder类
     * 
     * @author lihong06
     * @since 2014-1-23
     */
    public static class Builder {
        /** Context */
        private Context mContext;
        /** 当前缓存路径 */
        private String mCacheDir = null;
        /** 使用Disk缓存 */
        private boolean mUseDiskCache = false;
        /** 缓存最大百分比 */
        private float mMaxCachePercent = MAX_CACHE_PERCENT;
        
        /**
         * 构造实例
         * @return Builder对象
         */
        public static Builder newInstance(Context context) {
            return new Builder(context);
        }
        
        /**
         * 构造方法
         */
        private Builder(Context context) {
            mContext = context.getApplicationContext();
        }
        
        /**
         * 是否使用disk缓存
         * 
         * @param useDiskCache true/false
         * @return Builder对象
         */
        public Builder setUseDiskCache(boolean useDiskCache) {
            mUseDiskCache = useDiskCache;
            return this;
        }
        
        /**
         * disk缓存目录
         * 
         * @param cacheDir 目录
         * @return Builder对象
         */
        public Builder setDiskCacheDir(String cacheDir) {
            mCacheDir = cacheDir;
            return this;
        }
        
        /**
         * 设置缓存最大百分比
         * 
         * @param percent 百分比，必须在0.05f与0.8f之间
         * @return Builder对象
         */
        public Builder setMaxCachePercent(float percent) {
            mMaxCachePercent = percent;
            return this;
        }
        
        /**
         * 创建ImageLoader的实例
         * 
         * @param context context
         * @return ImageLoader
         */
        public ImageLoader build() {
            ImageLoader imageLoader = new ImageLoader(mContext);
            imageLoader.mCacheDir = mCacheDir;
            imageLoader.mUseDiskCache = mUseDiskCache;
            imageLoader.mMaxCachePercent = mMaxCachePercent;
            imageLoader.init();
            return imageLoader;
        }
    }
}
