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

import android.graphics.Bitmap;

/**
 * 该接口定义了加载Bitmap的行为，一般情况下，对应的Mode类可以去实现这个接口。
 * 
 * @author Li Hong
 * @since 2013-7-23
 */
public interface ILoadImage {
    /**
     * 加载一个bitmap
     * 
     * @return 加载的bitmap
     */
    public Bitmap loadImage();

    /**
     * 返回当前数据的URL
     * 
     * @return URL
     */
    public String getUrl();
}
