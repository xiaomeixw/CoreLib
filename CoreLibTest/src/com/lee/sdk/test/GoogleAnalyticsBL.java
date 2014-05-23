package com.lee.sdk.test;

import android.app.Activity;
import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

public class GoogleAnalyticsBL
{
    private static GoogleAnalyticsBL s_instance = null;
    
    public synchronized static GoogleAnalyticsBL getInstance()
    {
        return s_instance;
    }
    
    public synchronized static GoogleAnalyticsBL getInstance(Context context)
    {
        if (null == s_instance)
        {
            s_instance = new GoogleAnalyticsBL(context);
        }
        
        return s_instance;
    }
    
//    private Tracker mGaTracker;
//    private GoogleAnalytics mGaInstance;
    
    private GoogleAnalyticsBL(Context context)
    {
        // Get the GoogleAnalytics singleton. Note that the SDK uses
        // the application context to avoid leaking the current context.
        //mGaInstance = GoogleAnalytics.getInstance(context);
        //mGaTracker = mGaInstance.getTracker("UA-37656393-1");
        //mGaInstance.setDefaultTracker(mGaTracker);
        EasyTracker.getInstance().setContext(context);
    }
    
    public void sendView(String appScreen)
    {
        Tracker trackger = EasyTracker.getTracker();
        if (null != trackger)
        {
            trackger.sendView(appScreen);
        }
    }
    
    public void sendEvent(String category, String action, String label, Long value)
    {
        Tracker trackger = EasyTracker.getTracker();
        if (null != trackger)
        {
            trackger.sendEvent(category, action, label, value);
        }
    }
    
    public void sendException(String description, boolean fatal)
    {
        Tracker trackger = EasyTracker.getTracker();
        if (null != trackger)
        {
            trackger.sendException(description, fatal);
        }
    }
    
    public void dispatch()
    {
        //GAServiceManager.getInstance().dispatch();
        EasyTracker.getInstance().dispatch();
    }
    
    public String getActivityName(Activity activity)
    {
        String canonicalName = activity.getClass().getCanonicalName();

        return canonicalName;
    }
    
    public void activityStart(Activity activity)
    {
        //EasyTracker.getInstance().activityStart(activity); 
    }
    
    public void activityStop(Activity activity)
    {
        //EasyTracker.getInstance().activityStop(activity); 
    }
}
