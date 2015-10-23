package com.brodev.socialapp.view.base;

import android.os.Bundle;

import com.brodev.socialapp.view.BaseActivity;

import java.util.concurrent.atomic.AtomicBoolean;

public class BaseLogeableActivity extends BaseActivity implements QBLogeable {

    protected AtomicBoolean canPerformLogout = new AtomicBoolean(true);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(CAN_PERFORM_LOGOUT)) {
            canPerformLogout = new AtomicBoolean(savedInstanceState.getBoolean(CAN_PERFORM_LOGOUT));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(CAN_PERFORM_LOGOUT, canPerformLogout.get());
        super.onSaveInstanceState(outState);
    }

    //This method is used for logout action when Actvity is going to background
    @Override
    public boolean isCanPerformLogoutInOnStop() {
        return canPerformLogout.get();
    }
}