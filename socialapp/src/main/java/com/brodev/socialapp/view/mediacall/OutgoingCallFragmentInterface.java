package com.brodev.socialapp.view.mediacall;

/**
 * Created by Administrator on 7/24/2015.
 */
public interface OutgoingCallFragmentInterface {
    void onMic();
    void offMic();
    void onCam();
    void offCam();
    void switchCam();
    void switchSpeaker();
    void hungUpClick();
    void onLocalVideoViewCreated();
    void onRemoteVideoViewCreated();
}
