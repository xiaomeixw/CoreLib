package com.lee.sdk.test.lock;

import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.lee.sdk.lock.ChooseLockPattern;
import com.lee.sdk.lock.ChooseLockPattern.OnSaveLockPatternListener;
import com.lee.sdk.lock.ConfirmLockPattern;
import com.lee.sdk.lock.ConfirmLockPattern.OnConfirmLockPatternListener;
import com.lee.sdk.lock.LockPatternUtils;
import com.lee.sdk.lock.LockPatternView.Cell;
import com.lee.sdk.test.BaseFragmentActivity;
import com.lee.sdk.test.R;

public class LockPatternActivity extends BaseFragmentActivity {
    private ChooseLockPattern mChooseLockPattern;
    private ConfirmLockPattern mConfirmLockPattern;
    private ImageView mSwitcher;
    
    private LockPatternUtils mLockPatternUtils;
    private boolean mHasLockPattern;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLockPatternUtils = new LockPatternUtils(this);
        
        if (mLockPatternUtils.savedPatternExists()) {
            setupConfirmLockPattern();
        } else {
            setupContentView();
        }
    }
    
    private void setupConfirmLockPattern() {
        if (null == mConfirmLockPattern) {
            mConfirmLockPattern = new ConfirmLockPattern(this);
        }
        
        mConfirmLockPattern.setOnConfirmLockPatternListener(new OnConfirmLockPatternListener() {
            @Override
            public void onConfirmLockPatternAndClose(boolean confirm) {
                mHasLockPattern = true;
                setupContentView();
            }
        });
        
        setContentView(mConfirmLockPattern);
    }
    
    private void setupChooseLockPattern() {
        if (null == mChooseLockPattern) {
            mChooseLockPattern = new ChooseLockPattern(this);
        }
        
        mChooseLockPattern.setOnSaveLockPatternListener(new OnSaveLockPatternListener() {
            @Override
            public void onSaveChosenPatternAndClose(List<Cell> pattern) {
                mLockPatternUtils.saveLockPattern(pattern);
                mHasLockPattern = true;
                setupContentView();
            }
        });
        mChooseLockPattern.onCreate();
        setContentView(mChooseLockPattern);
    }
    
    private void setupContentView() {
        setContentView(R.layout.lock_pattern_layout);
        mSwitcher = (ImageView) findViewById(R.id.switcher);
        mSwitcher.setImageResource(R.drawable.checkbox_private);
        mSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = mSwitcher.isSelected();
                if (!isSelected) {
                    setupChooseLockPattern();
                } else {
                    mLockPatternUtils.clearLock();
                }
                mSwitcher.setSelected(!isSelected);
            }
        });
        mSwitcher.setSelected(mHasLockPattern);
    }
}
