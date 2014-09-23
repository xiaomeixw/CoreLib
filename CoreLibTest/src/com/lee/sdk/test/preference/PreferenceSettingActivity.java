/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.test.preference;

import android.os.Bundle;

import com.lee.sdk.app.BasePreferenceActivity;
import com.lee.sdk.test.R;
import com.lee.sdk.widget.preference.PreferenceFragment;

/**
 * 
 * @author lihong06
 * @since 2014-9-17
 */
public class PreferenceSettingActivity extends BasePreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
    
    @Override
    protected String getActivityTitle() {
        return getTitle().toString();
    }

    @Override
    protected PreferenceFragment getPreferenceFragment() {
        return new SettingFragment();
    }
    
    private class SettingFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);
        }
    }
}
