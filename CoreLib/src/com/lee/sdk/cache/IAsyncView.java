package com.lee.sdk.cache;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

//CHECKSTYLE:OFF

/**
 * This interface defines the methods for the View which to show the bitmap, typically 
 * is the image view.
 * 
 * <br>
 * NOTE:
 * Suggest caller uses {@link AsyncView} class which is extended from {@link IAsyncView}.
 * In future, this interface may be rename, in face, this interface is like listener or callback.
 * 
 * @author LiHong
 * 
 * @date 2012/10/25
 */
public interface IAsyncView {
    
    /**
     * Set the bitmap to the View.
     * 
     * @param bitmap The bitmap object.
     */
    public void setImageBitmap(Bitmap bitmap);
    
    /**
     * Set the drawable to the view, it is same with {@link #setImageBitmap(Bitmap bitmap)} method.
     * 
     * @param drawable
     */
    public void setImageDrawable(Drawable drawable);
    
    /**
     * Set the drawable to the view to save.
     * 
     * @param drawable
     */
    public void setAsyncDrawable(Drawable drawable);

    /**
     * Return the drawable object which is set by {@link #setAsyncDrawable()} method.
     * 
     * @return The object which just set by calling {@link #setAsyncDrawable()} method.
     */
    public Drawable getAsyncDrawable();
    
    /**
     * 是否支持GIF
     * 
     * @return true/false
     */
    public boolean isGifSupported();
}
//CHECKSTYLE:ON
