package com.lee.sdk.cache;

import java.util.ArrayList;

//CHECKSTYLE:OFF
/**
 * The Recycle pool.
 */
public class RecyclePool<T> {
    private ArrayList<T> mPool = new ArrayList<T>();
    private final int mPoolLimit;
    
    public RecyclePool(int poolLimit) {
        mPoolLimit = poolLimit;
    }
    
    /**
     * Recycle the item into the pool.
     * 
     * @param info
     */
    public synchronized void recycle(T info) {
        if (null != info) {
            if (mPool.size() >= mPoolLimit) {
                mPool.remove(mPool.size() - 1);
            }
            
            mPool.add(info);
        }
    }
    
    /**
     * Get the item from the pool.
     * 
     * @return the item in the pool, may be null.
     */
    public synchronized T get() {
        while (mPool.size() > 0) {
            T value = mPool.remove(mPool.size() - 1);
            if (null != value) {
                return value;
            }
        }
        
        return null;
    }
    
    /**
     * Clear the pool
     */
    public void clear() {
        mPool.clear();
    }
}
//CHECKSTYLE:ON
