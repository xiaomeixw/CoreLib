/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.test.widget;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.Toast;

import com.lee.sdk.cache.ImageLoader;
import com.lee.sdk.cache.ImageResizer.OnProcessDataListener;
import com.lee.sdk.task.Task;
import com.lee.sdk.task.Task.RunningStatus;
import com.lee.sdk.task.TaskManager;
import com.lee.sdk.task.TaskOperation;
import com.lee.sdk.test.ImageBrowserActivity;
import com.lee.sdk.test.utils.ImageSearchUtil;
import com.lee.sdk.test.utils.MediaInfo;
import com.lee.sdk.utils.ImageUtils;
import com.lee.sdk.utils.Utils;

/**
 * 
 * @author lihong06
 * @since 2014-10-12
 */
public class ImageGridView extends FrameLayout {
    private GridView mGridView = null;
    private ImageLoader mImageLoader = null;
    private ImageSearchUtil mSearchUtil = null;
    private ArrayList<MediaInfo> mDatas = null;
    private int mWidth = 100;
    private List<String> mFiles = null;
    private boolean mUseDiskCache = true;
    private OnGridViewItemClickListener mExternalItemClickListener;
    private OnCovertDataListener mExternalCovertDataListener;

    public ImageGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ImageGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageGridView(Context context) {
        super(context);
        init();
    }
    
    public ImageGridView(Context context, boolean useDiskCache) {
        super(context);
        mUseDiskCache = useDiskCache;
        init();
    }

    private void init() {
        final Context context = getContext();

        mWidth = (int) Utils.pixelToDp(context, mWidth);
        mGridView = new GridView(context);
        mGridView.setVerticalSpacing(ImageViewAdapter.SPACE);
        mGridView.setHorizontalSpacing(ImageViewAdapter.SPACE);
        mGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        mGridView.setPadding(5, 0, 5, 0);
        mGridView.setColumnWidth(mWidth);
        mGridView.setNumColumns(3);

        mImageLoader = ImageLoader.Builder.newInstance(context).setMaxCachePercent(0.3f)
                .setFadeInBitmap(true).setUseDiskCache(mUseDiskCache).setFadeInBitmap(true)
                .setMaxDiskCacheSize(1024 * 1024 * 100).build();
//        mImageLoader.setOnProcessBitmapListener(new OnProcessBitmapListener() {
//            @Override
//            public Bitmap onProcessBitmap(Object data) {
//                if (data instanceof MediaInfo) {
//                    return mSearchUtil.getImageThumbnail2((MediaInfo) data);
//                } else if (data instanceof String) {
//                    return ImageUtils.getBitmapFromNet(getContext(), 2, (String) data);
//                }
//                return null;
//            }
//        });
        
        mImageLoader.setOnProcessDataListener(new OnProcessDataListener() {
            @Override
            public Object onDecodeStream(Object data, InputStream is) {
                if (null != is) {
                    return ImageUtils.deocdeBitmap(is, 480, 480 * 800);
                }
                
                return null;
            }
        });
        
        ImageViewAdapter adapter = new ImageViewAdapter(context, mImageLoader);
        adapter.setUsePath(false);

        mSearchUtil = new ImageSearchUtil(context);
        mGridView.setAdapter(adapter);
        // mGridView.setOnScrollListener(new ScrollListenerImpl());
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaInfo info = mDatas.get(position);
                Toast.makeText(context, "Item " + position + " clicked!   path = " + info.getFullPath(),
                        Toast.LENGTH_SHORT).show();
                
                if (null != mExternalItemClickListener) {
                    mExternalItemClickListener.onItemClick(position, info);
                } else {
                    ImageBrowserActivity.launchImageBrowser(context, converData(mDatas), position);
                }
            }
        });

        addView(mGridView, new FrameLayout.LayoutParams(-1, -1));
    }

    public void setOnGridViewItemClickListener(OnGridViewItemClickListener listener) {
        mExternalItemClickListener = listener;
    }
    
    public void setOnCovertDataListener(OnCovertDataListener listener) {
        mExternalCovertDataListener = listener;
    }

    public interface OnGridViewItemClickListener {
        void onItemClick(int position, MediaInfo info);
    }
    
    public interface OnCovertDataListener {
        List<String> converData(List<MediaInfo> datas);
    }

    private List<String> converData(List<MediaInfo> datas) {
        if (null != mExternalCovertDataListener) {
            List<String> retDatas = mExternalCovertDataListener.converData(datas);
            if (null != retDatas) {
                return retDatas;
            }
        }
        
        if (null != mFiles) {
            return mFiles;
        }

        mFiles = new ArrayList<String>();
        for (MediaInfo info : datas) {
            mFiles.add(info.getFullPath());
        }

        return mFiles;
    }

    public void loadImageAsync() {
        loadImageAsync(null);
    }
    
    public void setData(ArrayList<String> datas) {
        ImageViewAdapter adapter = (ImageViewAdapter) mGridView.getAdapter();
        adapter.setUsePath(true);
        adapter.setIsSupportGif(true);
        if (null == mDatas) {
            mDatas = new ArrayList<MediaInfo>();
        }
        
        for (String str : datas) {
            MediaInfo info = new MediaInfo();
            info.setFullPath(str);
            mDatas.add(info);
        }
        
        adapter.setData(mDatas);
        adapter.notifyDataSetChanged();
    }

    public void loadImageAsync(final FileFilter filter) {
        // Start task to load image data.
        new TaskManager().next(new Task(RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("Load image");
                progressDialog.setMessage("Load image from SD Card, please wait...");
                progressDialog.setCancelable(false);

                if (!progressDialog.isShowing()) {
                    progressDialog.show();
                }

                operation.setTaskParams(new Object[] { progressDialog });

                return operation;
            }
        }).next(new Task(RunningStatus.WORK_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                ArrayList<MediaInfo> datas = mSearchUtil.getImagesFromSDCard(false);

                if (null != datas) {
                    mDatas = new ArrayList<MediaInfo>();
                    if (null != filter) {
                        for (MediaInfo info : datas) {
                            if (filter.accept(new File(info.getFullPath()))) {
                                mDatas.add(info);
                            }
                        }
                    } else {
                        mDatas.addAll(datas);
                    }
                }

                return operation;
            }
        }).next(new Task(RunningStatus.UI_THREAD) {
            @Override
            public TaskOperation onExecute(TaskOperation operation) {
                Object[] params = operation.getTaskParams();
                ProgressDialog mProgressDialog = (ProgressDialog) params[0];
                mProgressDialog.dismiss();

                ImageViewAdapter adapter = (ImageViewAdapter) mGridView.getAdapter();
                adapter.setData(mDatas);
                adapter.notifyDataSetChanged();

                if (mDatas.size() == 0) {
                    Toast.makeText(getContext(), "没有找到图片", Toast.LENGTH_SHORT).show();
                }

                return operation;
            }
        }).execute();
    }
}
