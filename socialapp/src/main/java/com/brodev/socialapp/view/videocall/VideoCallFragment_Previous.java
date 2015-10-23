package com.brodev.socialapp.view.videocall;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.brodev.socialapp.utils.Consts;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.brodev.socialapp.view.mediacall.CallActivity;
import com.brodev.socialapp.view.mediacall.OutgoingCallFragment;
import com.mypinkpal.app.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;

public class VideoCallFragment_Previous extends OutgoingCallFragment {

    public static final String TAG = VideoCallFragment.class.getSimpleName();
    //    private QBGLVideoView videoView;
    private ImageView imgMyCameraOff;
    private ToggleButton cameraOffButton;
    private ImageButton switchCameraButton;
    //    private boolean isVideoEnabled = true;
//    private boolean isCameraEnabled = true;
    private QBGLVideoView localVideoView;
    private QBGLVideoView remoteVideoView;
    private CameraState cameraState = null;

    @Override
    protected int getContentView() {
        return R.layout.activity_video_call_previous;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraState == CameraState.DISABLED_FROM_USER){
            toggleCamera(false);
        } else if (cameraState == CameraState.ENABLED_FROM_USER || cameraState == CameraState.NONE) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        toggleCamera(false);
        if (cameraState == null) {
            cameraState = CameraState.NONE;
        }
        super.onPause();
    }

    @Override
    protected void initUI(View rootView) {

        Log.d(TAG, "initUI()");

        localVideoView = (QBGLVideoView) rootView.findViewById(R.id.localVideoView);
        remoteVideoView = (QBGLVideoView) rootView.findViewById(R.id.remoteVideoView);

        ((CallActivity)getActivity()).setLocalVideoView(localVideoView);
        ((CallActivity)getActivity()).setRemoteVideoView(remoteVideoView);

        imgMyCameraOff = (ImageView) rootView.findViewById(R.id.imgMyCameraOff);

        cameraOffButton = (ToggleButton) rootView.findViewById(R.id.cameraOffButton);
        cameraOffButton.setOnClickListener(this);

        //switchCameraButton = (ImageButton) rootView.findViewById(R.id.switchCameraButton);
        //switchCameraButton.setOnClickListener(this);
        if (/*opponent*/ opponent != null) {
            SharedPreferences profilepref = getActivity().getSharedPreferences("mypinkpal_friendlist", Context.MODE_PRIVATE);
            //TextView timerTextView=((TextView) rootView.findViewById(R.id.timerTextView));
           // timerTextView.setVisibility(View.INVISIBLE);
            ((TextView) rootView.findViewById(R.id.name_textview_video)).setText(/*opponent*/ opponent.getFullName());
            //TextView name_textview_process=((TextView) rootView.findViewById(R.id.name_textview_process));
            RoundedImageView avatarView = (RoundedImageView) rootView.findViewById(R.id.avatar_imageview_video);
            avatarView.setOval(true);
            if (!TextUtils.isEmpty(/*opponent*/ profilepref.getString(opponent.getEmail(), ""))) {
                Log.d("imageURL1",opponent.getAvatarUrl());
                ImageLoader.getInstance().displayImage(profilepref.getString(opponent.getEmail(), ""),
                        avatarView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
            }
        }

        super.initUI(rootView);
    }

    @Override
    public void setActionButtonsEnability(boolean enability) {
        super.setActionButtonsEnability(enability);

        cameraOffButton.setEnabled(enability);
        //switchCameraButton.setEnabled(enability);

        // inactivate toggle buttons
        cameraOffButton.setActivated(enability);
        //switchCameraButton.setActivated(enability);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.cameraOffButton:
                if (cameraState != CameraState.DISABLED_FROM_USER) {
                    toggleCamera(false);
                    cameraState = CameraState.DISABLED_FROM_USER;
                } else {
                    toggleCamera(true);
                    cameraState = CameraState.ENABLED_FROM_USER;
                }
                break;
            /*case R.id.switchCameraButton:
                switchCamera();
                break;*/
            default:
                break;
        }
    }

    private void toggleCamera(boolean isCameraEnabled) {
        if (outgoingCallFragmentInterface != null){
            DisplayMetrics displaymetrics = new DisplayMetrics();
            displaymetrics.setToDefaults();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            ViewGroup.LayoutParams layoutParams = imgMyCameraOff.getLayoutParams();

//            ViewGroup.LayoutParams videoViewLayoutParams = videoView.getLayoutParams();
//            int videoViewHeight = videoView.getHeight();
//            int videoViewWidth = videoView.getWidth();

            int videoViewHeight = localVideoView.getHeight();
            int videoViewWidth = localVideoView.getWidth();

//        Log.d(TAG, "height - videoViewHeight " + (height - videoViewHeight) + " width-videoViewWidth " + (width - videoViewWidth) + "");



//            layoutParams.height = (int)Math.ceil(((/*height*/videoViewHeight / 100) * 33));
//            layoutParams.width = (int)Math.ceil(((/*width*/ videoViewWidth / 100) * 33));

            layoutParams.height = videoViewHeight;
            layoutParams.width = videoViewWidth;
            imgMyCameraOff.setLayoutParams(layoutParams);

            if (!isCameraEnabled){
//                isVideoEnabled = false;
                //switchCameraButton.setVisibility(View.INVISIBLE);
                cameraOffButton.setChecked(true);
                imgMyCameraOff.setVisibility(View.VISIBLE);
                Log.d(TAG, "Camera disabled");
                outgoingCallFragmentInterface.offCam();
            } else {
//                isVideoEnabled = true;
                //switchCameraButton.setVisibility(View.VISIBLE);
                cameraOffButton.setChecked(false);
                imgMyCameraOff.setVisibility(View.INVISIBLE);
                Log.d(TAG, "Camera enabled");
                outgoingCallFragmentInterface.onCam();
            }
        }
    }

    private void switchCamera(){
        if (outgoingCallFragmentInterface != null) {
            outgoingCallFragmentInterface.switchCam();
            Log.d(TAG, "Camera switched!");
        }
    }

    private enum CameraState {
        NONE, DISABLED_FROM_USER, ENABLED_FROM_USER
    }
}