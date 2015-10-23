package com.brodev.socialapp.view.mediacall;


import android.os.Handler;
import android.widget.TextView;

import com.quickblox.q_municate_core.utils.ConstsCore;

public class TimeUpdater implements Runnable {

    private TextView timeTextView;
    private Handler handler;
    private int second;
    private int minute;
    private int totaltime;

    public TimeUpdater(TextView timeTextView, Handler handler) {
        this.timeTextView = timeTextView;
        this.handler = handler;
    }
    public int totaltime(){
        return totaltime;
    }
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setTextView(TextView timeTextView) {
        this.timeTextView = timeTextView;
    }

    @Override
    public void run() {
        totaltime++;
        second++;
        if (second == 60) {
            second = 0;
            minute++;
        }
        timeTextView.setText(String.format("%02d:%02d", minute, second));
        handler.removeCallbacks(this);
        handler.postDelayed(this, ConstsCore.SECOND);
    }

}
