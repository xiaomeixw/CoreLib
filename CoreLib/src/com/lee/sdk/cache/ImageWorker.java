/*
 * Copyright (C) 2012 The Android Open Source Project
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;

//CHECKSTYLE:OFF

/**
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public abstract class ImageWorker {
    /**
     * The load image listener. 
     * 
     * @author Li Hong
     * @since 2013-7-24
     */
    public interface OnLoadImageListener {
        /**
         * Called when finished to load image.
         * 
         * @param data the data you just passed to load image.
         * @param bitmap the loaded bitmap, the bitmap may be null. if it is null, the
         *        bitmap loading operation may fail.
         */
        public void onLoadImage(Object data, Object bitmap);
    }
    
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 200;

    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private boolean mUseCache = true;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    protected Resources mResources;

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;
    private static final int MESSAGE_CLEAR_BY_DATA = 4;
    private static final boolean DEBUG = true & BuildConfig.DEBUG;
    
    private WeakReference<OnLoadImageListener> mListener;

    protected ImageWorker(Context context) {
        mResources = context.getResources();
    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and disk
     * cache will be used if an {@link ImageCache} has been set using
     * {@link ImageWorker#setImageCache(ImageCache)}. If the image is found in the memory cache, it
     * is set immediately, otherwise an {@link AsyncTask} will be created to asynchronously load the
     * bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     * 
     * @return the Bitmap object, if the bitmap associated this data exist in cache, it 
     * will return it, otherwise return null;
     */
    public boolean loadImage(Object data, IAsyncView imageView) {
        return loadImage(data, imageView, null);
    }
    
    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and disk
     * cache will be used if an {@link ImageCache} has been set using
     * {@link ImageWorker#setImageCache(ImageCache)}. If the image is found in the memory cache, it
     * is set immediately, otherwise an {@link AsyncTask} will be created to asynchronously load the
     * bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     * @param listener The load image listener, this listener only be used when load bitmap in work thread.
     * 
     * @return the Bitmap object, if the bitmap associated this data exist in cache, it 
     * will return it, otherwise return null;
     */
    public boolean loadImage(Object data, IAsyncView imageView, OnLoadImageListener listener) {
        if (data == null) {
            return false;
        }
        if (DEBUG) {
            Log.d(TAG, "ImageWorker loadImage data = " + data);
        }
        Bitmap bitmap = null;
        boolean succeed = false;

        if (mImageCache != null) {
            bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
            if (DEBUG) {
                Log.d(TAG, "get bitmap from memcache data = " + data);
            }
        }

        if (bitmap != null) {
            // Bitmap found in memory cache
            imageView.setImageBitmap(bitmap);
            // Here set the drawable to null.
            imageView.setAsyncDrawable(null);
            
            // Added by LiHong at 2013/07/24 begin =======
            perfermOnLoadImage(data, bitmap);
            if (DEBUG) {
                Log.d(TAG, "loadImage try to nofity listener data = " + data);
            }
            if (null != listener) {
                listener.onLoadImage(data, bitmap);
                if (DEBUG) {
                    Log.d(TAG, "loadImage after nofity listener data = " + data);
                }
            }
            // Added by LiHong at 2013/07/24 end =========
            
            succeed = true;
            
//        } else if (cancelPotentialWork(data, imageView)) {
//            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
//            final AsyncDrawable asyncDrawable =
//                    new AsyncDrawable(mResources, mLoadingBitmap, task);
//            imageView.setImageBitmap(mLoadingBitmap);
//            imageView.setAsyncDrawable(asyncDrawable);
//
//            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
//            // framework and slightly modified. Refer to the docs at the top of the class
//            // for more info on what was changed.
//            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
//        }
        }  else {
            // NOTE: we will NOT create a new AsyncDrawable object when cancel potential work,
            // we can reuse it.
            boolean createTask = false;
            AsyncDrawable asyncDrawable = getAsyncDrawable(imageView);
            if (null != asyncDrawable) {
                if (asyncDrawable.cancelPotentialWork(data)) {
                    createTask = true;
                }
            } else {
                createTask = true;
            }
            if (DEBUG) {
                Log.d(TAG, "loadImage createTask = " + data);
            }
            if (createTask) {
                if (null == asyncDrawable) {
                    asyncDrawable = new AsyncDrawable(mResources, mLoadingBitmap);
                }
                
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                asyncDrawable.setWorkerTask(task);
                // Set the listener.
                if (DEBUG) {
                    Log.d(TAG, "loadImage setLoadImageListener listener = " + listener);
                }
                asyncDrawable.setLoadImageListener(listener);
                imageView.setImageBitmap(mLoadingBitmap);
                imageView.setAsyncDrawable(asyncDrawable);
                
                // NOTE: This uses a custom version of AsyncTask that has been pulled from the
                // framework and slightly modified. Refer to the docs at the top of the class
                // for more info on what was changed.
                //task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
                if (DEBUG) {
                    Log.d(TAG, "BitmapWorkerTask start " + data);
                }
                succeed = true;
            }
        }
        
        return succeed;
    }

    /**
     * Get the bitmap from cache.
     * 
     * @param data
     * 
     * @return
     */
    public Bitmap getBitmapFromCache(Object data) {
        Bitmap bitmap = null;

        if (mImageCache != null) {
            bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
        }
        
        return bitmap;
    }
    
    /**
     * Adds a bitmap to both memory and disk cache.
     * 
     * @param data Unique identifier for the bitmap to store
     * @param bitmap The bitmap to store
     */
    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (null != mImageCache) {
            mImageCache.addBitmapToCache(data, bitmap);
        }
    }
    
    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    /**
     * Adds an {@link ImageCache} to this worker in the background (to prevent disk access on UI
     * thread).
     * @param cacheParams
     */
    public void addImageCache(ImageCache.ImageCacheParams cacheParams) {
        mImageCacheParams = cacheParams;
        ImageCache cache = new ImageCache(mImageCacheParams);
        setImageCache(cache);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }
    
    /**
     * Sets the {@link ImageCache} object to use with this ImageWorker. Usually you will not need
     * to call this directly, instead use {@link ImageWorker#addImageCache} which will create and
     * add the {@link ImageCache} object in a background thread (to ensure no disk access on the
     * main/UI thread).
     *
     * @param imageCache
     */
    public void setImageCache(ImageCache imageCache) {
        mImageCache = imageCache;
    }

    /**
     * Get the image cache object.
     *  
     * @return imageCache
     */
    protected ImageCache getImageCache() {
        return mImageCache;
    }
    
    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }
    
    /**
     * If set to true, the bitmap will add to cache for reuse.
     * 
     * @param useCache
     */
    public void setUseCache(boolean useCache) {
        mUseCache = useCache;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
    }
    
    /**
     * Set the load image listener.
     * 
     * @param listener
     */
    public void setOnLoadImageListener(OnLoadImageListener listener) {
        if (null == listener) {
            mListener = null;
        } else {
            mListener = new WeakReference<OnLoadImageListener>(listener);
        }
    }

    private void perfermOnLoadImage(Object data, Object bitmap) {
        if (null != mListener) {
            OnLoadImageListener listener = mListener.get();
            if (null != listener) {
                listener.onLoadImage(data, bitmap);
            }
        }
    }
    
    /**
     * Subclasses should override this to define any processing or work that must happen to produce
     * the final bitmap. This will be executed in a background thread and be long running. For
     * example, you could resize a large bitmap here, or pull down an image from the network.
     *
     * @param data The data to identify which image to process, as provided by
     *            {@link ImageWorker#loadImage(Object, IAsyncView)}
     * @return The processed bitmap
     */
    protected abstract Bitmap decodeBitmap(Object data);
    
    /**
     * Process the data and return the bitmap or input stream.
     * 
     * @param data The data to identify which image to process, as provided by
     *            {@link ImageWorker#loadImage(Object, IAsyncView)}
     * @return bitmap or input stream
     */
    protected abstract InputStream downloadStream(Object data);
    
    /**
     * Decode the input stream, return the bitmap or drawble, if the returned value is drawable, typically it is for GIF.
     * 
     * @param object data
     * @param is input stream.
     * @param isGifSupported 是否支持GIF，true/false
     * @return bitmap or drawable
     */
    protected abstract Object decodeStream(Object data, InputStream is, boolean isGifSupported);

    /**
     * Cancels any pending work attached to the provided ImageView.
     * @param imageView
     */
    public static void cancelWork(IAsyncView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            if (BuildConfig.DEBUG) {
                final Object bitmapData = bitmapWorkerTask.mData;
                Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
            }
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public static boolean cancelPotentialWork(Object data, IAsyncView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.mData;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "cancelPotentialWork - cancelled work for " + data);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(IAsyncView imageView) {
        if (imageView != null) {
            //final Drawable drawable = imageView.getDrawable();
            final Drawable drawable = imageView.getAsyncDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
    
    private static OnLoadImageListener getLoadImageListener(IAsyncView imageView) {
        if (imageView != null) {
            //final Drawable drawable = imageView.getDrawable();
            final Drawable drawable = imageView.getAsyncDrawable();
            if (DEBUG) {
                Log.d(TAG, "getLoadImageListener drawable = " + drawable);
            }
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                OnLoadImageListener listener = asyncDrawable.getLoadImageListener();
                asyncDrawable.setLoadImageListener(null);
                return listener;
            }
        }
        return null;
    }
    
    /**
     * Get the drawable from the IAsyncView.
     * 
     * @param imageView
     * @return
     */
    private static AsyncDrawable getAsyncDrawable(IAsyncView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getAsyncDrawable();
            if (drawable instanceof AsyncDrawable) {
                return (AsyncDrawable)drawable;
            }
        }
        
        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<Object, Void, Object> {
        /** 是否需要使用输入流，还是直接处理bitmap */
        private boolean mForInputStream = true;
        /** 是否支持GIF，如果文件是GIF，则返回GifDrawable，否则返回Bitmap */
        private boolean mIsGifSupported = true;
        private Object mData;
        private final WeakReference<IAsyncView> imageViewReference;

        public BitmapWorkerTask(IAsyncView imageView) {
            imageViewReference = new WeakReference<IAsyncView>(imageView);
            mIsGifSupported = imageView.isGifSupported();
        }

        /**
         * Background processing.
         */
        @Override
        protected Object doInBackground(Object... params) {
            // 
            // NOTE: 
            // 这里有一个严重问题：原来的缓存逻辑都是直接从网络流中decode为Bitmap，然后再将bitmap compress到文件中,
            // 在这种情况下，图片可能会失真，如果实际图片带有透明度的话，保存到文件中，透明度的信息可能丢失，导致下次
            // 从文件缓存中读取出来，透明的部分就会显示黑色。
            // 解决方案：将文件写入流中，然后再从文件中读取，这样的好处是不会丢失图片信息，其次是在解析时可以计算sample size
            // 防止 OOM，因上，建议mForInputStream为true。
            //
            if (mForInputStream) {
                Object value = doInBackgroundForStream(params);
                return value;
            }
            
            return doInBackgroundForBitmap(params);
        }

        /**
         * Background processing.
         * 
         * @param data data
         */
        private Object doInBackgroundForBitmap(Object... params) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - starting work");
            }

            mData = params[0];
            final String dataString = String.valueOf(mData);
            Bitmap bitmap = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(dataString);
            }

            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = decodeBitmap(mData);
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null && mImageCache != null) {
                // If use cache, we add the bitmap to cache. 
                if (mUseCache) {
                    mImageCache.addBitmapToCache(dataString, bitmap);
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - finished work");
            }

            return bitmap;
        }
        
        /**
         * Background processing.
         * 
         * @param data data
         */
        private Object doInBackgroundForStream(Object... params) {
            mData = params[0];
            final String dataString = String.valueOf(mData);
            InputStream inputStream = null;
            Bitmap bitmap = null;
            
            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                inputStream = mImageCache.getStreamFromDiskCache(dataString);
            }
            
            boolean fromDiskCache = (null != inputStream);

            // Modified:
            // Read the input stream from cache and process it by the subclasses.
            //
            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (inputStream == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                inputStream = downloadStream(mData);
            }
            
            if (inputStream != null && mImageCache != null) {
                if (!fromDiskCache) {
                    // If use cache, we add the input stream to cache. 
                    if (mUseCache) {
                        mImageCache.addStreamToCache(dataString, inputStream);
                        // Fetch the stream
                        inputStream = mImageCache.getStreamFromDiskCache(dataString);
                    }
                }
            }
            
            Object retData = null;
            if (null != inputStream) {
                retData = decodeStream(mData, inputStream, mIsGifSupported);
            }
            
            if (retData instanceof Bitmap) {
                bitmap = (Bitmap) retData;
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null && mImageCache != null) {
                // If use cache, we add the bitmap to cache. 
                if (mUseCache) {
                    mImageCache.addBitmapToCache(dataString, bitmap, false);
                }
            }
            
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - finished work,  return data = " + retData);
            }

            return retData;
        }
        
        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Object data) {
            if (mForInputStream) {
                if (data instanceof Bitmap) {
                    onPostExecuteForBitmap(data);
                } else {
                    onPostExecuteForStream(data);
                }
            } else {
                onPostExecuteForBitmap(data);
            }
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        private void onPostExecuteForBitmap(Object data) {
            Bitmap bitmap = null;
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                bitmap = null;
                data = null;
            }
            
            bitmap = (Bitmap) data;

            final IAsyncView imageView = getAttachedImageView();
            // Find the listener.
            final OnLoadImageListener listener = getLoadImageListener(imageView);
            if (DEBUG) {
                Log.d(TAG, "onPostExecute getLoadImageListener listener = " + listener);
            }
            if (bitmap != null && imageView != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onPostExecute - setting bitmap");
                }
                
                setImageBitmap(imageView, bitmap);
            } 
            
            // Added by LiHong at 2013/03/04 begin ===========
            if (null != imageView) {
                imageView.setAsyncDrawable(null);
            }
            // Added by LiHong at 2013/03/04 end =============
            
            // Added by LiHong at 2013/07/24 begin =======
            if (null != listener) {
                listener.onLoadImage(mData, bitmap);
            }
            
            perfermOnLoadImage(mData, bitmap);
            // Added by LiHong at 2013/07/24 end =========
        }
        
        /**
        * Once the image is processed, associates it to the imageView
        */
        private void onPostExecuteForStream(Object data) {
           Drawable drawable = null;
           
           // if cancel was called on this task or the "exit early" flag is set then we're done
           if (isCancelled() || mExitTasksEarly) {
               data = null;
           }
           
           if (data instanceof Drawable) {
               drawable = (Drawable) data;
           }
           
           final IAsyncView imageView = getAttachedImageView();
           // Find the listener.
           final OnLoadImageListener listener = getLoadImageListener(imageView);
           
           if (null != drawable && imageView != null) {
               if (BuildConfig.DEBUG) {
                   Log.d(TAG, "onPostExecute - setting drawable");
               }
               
               setImageDrawable(imageView, drawable);
           }
           
           // Added by LiHong at 2013/03/04 begin ===========
           if (null != imageView) {
               imageView.setAsyncDrawable(null);
           }
           // Added by LiHong at 2013/03/04 end =============
           
           // Added by LiHong at 2013/07/24 begin =======
           if (null != listener) {
               listener.onLoadImage(mData, data);
           }
           
           perfermOnLoadImage(mData, data);
           // Added by LiHong at 2013/07/24 end =========
       }

        @Override
        protected void onCancelled(Object bitmap) {
            super.onCancelled(bitmap);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private IAsyncView getAttachedImageView() {
            final IAsyncView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable {
        private WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
        private WeakReference<OnLoadImageListener> loadImageListenerReference;

        public AsyncDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }
        
        @SuppressWarnings("unused")
        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }
        
        public void setWorkerTask(BitmapWorkerTask bitmapWorkerTask) {
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }
        
        public void setLoadImageListener(OnLoadImageListener listener) {
            if (null != listener) {
                loadImageListenerReference = new WeakReference<OnLoadImageListener>(listener);
            } else {
                loadImageListenerReference = null;
            }
        }
        
        public OnLoadImageListener getLoadImageListener() {
            if (null != loadImageListenerReference) {
                return loadImageListenerReference.get();
            }
            
            return null;
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
        
        public boolean cancelPotentialWork(Object data) {
            BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask();
            if (bitmapWorkerTask != null) {
                final Object bitmapData = bitmapWorkerTask.mData;
                if (bitmapData == null || !bitmapData.equals(data)) {
                    bitmapWorkerTask.cancel(true);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "cancelPotentialWork - cancelled work for " + data);
                    }
                } else {
                    // The same work is already in progress.
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Called when the processing is complete and the final bitmap should be set on the ImageView.
     *
     * @param imageView
     * @param bitmap
     */
    private void setImageBitmap(IAsyncView imageView, Bitmap bitmap) {
        if (mFadeInBitmap) {
            // Transition drawable with a transparent drwabale and the final bitmap
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[] {
                            new ColorDrawable(android.R.color.transparent),
                            new BitmapDrawable(mResources, bitmap)
                    });
            // Set background to loading bitmap
            /*
            imageView.setBackgroundDrawable(
                    new BitmapDrawable(mResources, mLoadingBitmap));
             */
            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }
    
    /**
     * Called when the processing is complete and the final bitmap should be set on the ImageView.
     *
     * @param imageView
     * @param bitmap
     */
    private void setImageDrawable(IAsyncView imageView, Drawable drawable) {
        if (mFadeInBitmap) {
            // Transition drawable with a transparent drwabale and the final bitmap
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[] {
                            new ColorDrawable(android.R.color.transparent),
                            drawable
                    });
            // Set background to loading bitmap
            /*
            imageView.setBackgroundDrawable(
                    new BitmapDrawable(mResources, mLoadingBitmap));
             */
            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }
    
    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
                case MESSAGE_CLEAR_BY_DATA:
                    String data = (String)params[1];
                    clearCacheInternal(data);
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    public void clearCacheInternal() {
        clearCacheInternal(true);
    }
    
    public void clearCacheInternal(boolean clearDiskCache) {
        if (mImageCache != null) {
            mImageCache.clearCache(clearDiskCache);
        }
    }
    
    public void clearCacheInternal(String data) {
        if (null != mImageCache) {
            mImageCache.clearCache(data);
        }
    }
    
    public void clearDiskCache(String data) {
        if (null != mImageCache) {
            mImageCache.clearDiskCache(data);
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }
    
    /**
     * Get the bitmap count in the memory cache.
     * 
     * @return the size
     */
    public int getBitmapSizeInMemCache() {
        if (null != mImageCache) {
            return mImageCache.getBitmapSizeInMemCache();
        }
        
        return 0;
    }
    
    /**
     * Clear the cache of specified data, this method only clear the memory cache, it will NOT
     * clear the disk cache.
     *
     * @param data Unique identifier for which item to get
     */
    public void clearCache(String data) {
        new CacheAsyncTask().execute(MESSAGE_CLEAR_BY_DATA, data);
    }
    
    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }
    
    /**
     * 检查Disk cache中是否有data对应的bitmap
     * 
     * @param data Unique identifier for which item to get
     * @returntrue if found in Disk Cache, false otherwise
     */
    public boolean hasBitmapInDiskCache(Object data) {
        if (mImageCache != null) {
            return mImageCache.hasBitmapInDiskCache(String.valueOf(data));
        }
        return false;
    }
}
//CHECKSTYLE:ON
