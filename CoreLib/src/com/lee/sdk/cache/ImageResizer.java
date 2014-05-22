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

import android.content.Context;
import android.graphics.Bitmap;

//CHECKSTYLE:OFF

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources given a target width
 * and height. Useful for when the input images might be too large to simply load directly into
 * memory.
 */
public class ImageResizer extends ImageWorker {

    // Added by LiHong at 2012/10/25 begin ===========
    /**
     * This interface defines the methods for caller to load bitmap from the specified data. 
     * 
     * @author Li Hong
     */
    public interface OnProcessBitmapListener  {
        
        /**
         * Process the passed data and return the bitmap.
         * 
         * @param data The data which you set the the image worker.
         * 
         * @return the bitmap object.
         */
        Bitmap onProcessBitmap(Object data);
    }
    
    /**
     * The listener.
     */
    protected OnProcessBitmapListener mListener;

    /**
     * Set the listener.
     * 
     * @param listener The OnProcessBitmapListener object.
     */
    public void setOnProcessBitmapListener(OnProcessBitmapListener listener) {
        mListener = listener;
    }
    
    // Added by LiHong at 2012/10/25 end =============
    
    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context context
     */
    public ImageResizer(Context context) {
        super(context);
    }

    /**
     * @see com.baidu.searchbox.discovery.picture.cache.ImageWorker#processBitmap(java.lang.Object)
     */
    @Override
    protected Bitmap processBitmap(Object data) {
        
        if (null != mListener) {
            return mListener.onProcessBitmap(data);
        }
        
        return null;
    }
}
//CHECKSTYLE:ON
