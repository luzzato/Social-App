package com.brodev.socialapp.view.mediacall;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.fragment.BROADCAST;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.view.base.BaseFragment;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.mypinkpal.app.R;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCException;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientConnectionCallbacks;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public abstract class OutgoingCallFragment extends BaseFragment implements View.OnClickListener, QBRTCClientConnectionCallbacks {

    public static final String TAG = "LCYCLE" + OutgoingCallFragment.class.getSimpleName();
    private static final String CALL_INTEGRATION = "CALL_INTEGRATION";
    protected User opponent;
    private ConstsCore.CALL_DIRECTION_TYPE call_direction_type;
    private boolean bounded;
    protected int opponentcallingid;
    private QBService service;
    private Timer callTimer;
    private String sessionId;
    private int callingtime;
    private int callingtime1;
    private int localcallingtime=0;

    protected OutgoingCallFragmentInterface outgoingCallFragmentInterface;


    private ArrayList<Integer> opponents;
    private int startReason;
    private QBRTCSessionDescription sessionDescription;

    private Map<String, String> userInfo;
    //    private boolean isVideoEnabled = true;
    private boolean isAudioEnabled = true;
    private List<QBUser> allUsers = new ArrayList<>();
    private boolean isMessageProcessed;
    private QBRTCTypes.QBConferenceType call_type;
    private ToggleButton muteDynamicButton;
    private ImageButton stopСallButton;
    private ToggleButton muteMicrophoneButton;
    private TextView timerTextView;
    private TextView name_textview_process;
    private TextView name_textview_video;
    private ImageView pinkpallogo_video;
    private TextView opponentcalling;
    private RoundedImageView avatar_imageview_video;
    private QBGLVideoView qbgllocalVideoView;
    private QBGLVideoView qbglremoteVideoView;
    private Handler handler;
    private TimeUpdater updater;
    protected QBVideoChatHelper videoChatHelper;
    private IntentFilter intentFilter;
    private AudioStreamReceiver audioStreamReceiver;
    protected boolean callIsStarted;
    private Handler showIncomingCallWindowTaskHandler;
    private Runnable showIncomingCallWindowTask;
    private NetworkUntil networkUntil;


    protected void initUI(View rootView) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment initUI ");
        timerTextView = (TextView) rootView.findViewById(R.id.timerTextView);
        name_textview_process = (TextView) rootView.findViewById(R.id.name_textview_process);
        name_textview_video=(TextView)rootView.findViewById(R.id.name_textview_video);
        opponentcalling=(TextView)rootView.findViewById(R.id.opponentcalling);
        pinkpallogo_video=(ImageView)rootView.findViewById(R.id.pinkpallogo_video);
        avatar_imageview_video=(RoundedImageView)rootView.findViewById(R.id.avatar_imageview_video);
        qbgllocalVideoView=(QBGLVideoView)rootView.findViewById(R.id.localVideoView);
        qbglremoteVideoView=(QBGLVideoView)rootView.findViewById(R.id.remoteVideoView);
        if (updater != null) {
            updater.setTextView(timerTextView);
        }


        muteDynamicButton = (ToggleButton) rootView.findViewById(R.id.muteDynamicButton);
        muteDynamicButton.setOnClickListener(this);

        stopСallButton = (ImageButton) rootView.findViewById(R.id.stopСallButton);
        stopСallButton.setOnClickListener(this);

        muteMicrophoneButton = (ToggleButton) rootView.findViewById(R.id.muteMicrophoneButton);
        muteMicrophoneButton.setOnClickListener(this);

        setActionButtonsEnability(false);
    }

    public void setActionButtonsEnability(boolean enability) {

        muteDynamicButton.setEnabled(enability);
        muteMicrophoneButton.setEnabled(enability);

        // inactivate toggle buttons
        muteDynamicButton.setActivated(enability);
        muteMicrophoneButton.setActivated(enability);
    }


    // ----------------------------- ConnectionState callbacks -------------------------- //

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {
        ((CallActivity) getActivity()).cancelPlayer();                   // надо пересмотреть
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "OutgoingCallFragment onConnectedToUser ");

                ((CallActivity) getActivity()).stopIncomeCallTimer();

                startTimer(timerTextView);
                timerTextView.setVisibility(View.VISIBLE);
                if(name_textview_process!=null) {
                    name_textview_process.setVisibility(View.INVISIBLE);
                }
                if(pinkpallogo_video!=null){
                    pinkpallogo_video.setVisibility(View.INVISIBLE);
                }
                if(avatar_imageview_video!=null){
                    avatar_imageview_video.setVisibility(View.INVISIBLE);
                }
                if(name_textview_video!=null){
                    name_textview_video.setVisibility(View.INVISIBLE);
                }
                if(opponentcalling!=null){
                    opponentcalling.setVisibility(View.INVISIBLE);
                }
                if(qbgllocalVideoView!=null){
                    // qbgllocalVideoView.setVisibility(View.INVISIBLE);
                    //View view_instance = (View)findViewById(R.id.nutrition_bar_filled);

                    RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) qbgllocalVideoView.getLayoutParams();
                    params.width= (int) getResources().getDimension(R.dimen.localVideoViewWidth);
                    params.height= (int) getResources().getDimension(R.dimen.localVideoViewHeight);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
                    params.setMargins(0,20,20,0);
                    qbgllocalVideoView.setLayoutParams(params);

                }
                //qbgllocalVideoView.setSize(QBGLVideoView.Endpoint.LOCAL,200,300);
                if(qbglremoteVideoView!=null) {
                    qbglremoteVideoView.setVisibility(View.VISIBLE);
                }

                setActionButtonsEnability(true);

                QBGLVideoView localVideoView = ((CallActivity) getActivity()).getLocalVideoView();
                if (localVideoView != null) {
//                    localVideoView.setVideoViewOrientation(QBGLVideoView.ORIENTATION_MODE.portrait.getDegreeRotation());
                }

                QBGLVideoView remoteView = ((CallActivity) getActivity()).getRemoteVideoView();
                if (remoteView != null) {
//                    remoteView.setVideoViewOrientation(QBGLVideoView.ORIENTATION_MODE.portrait.getDegreeRotation());
                }
            }
        });
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment onConnectionClosedForUser ");
        ((CallActivity) getActivity()).stopIncomeCallTimer();
        Log.d("myconnectiontime1", "myconnectiontime");
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "OutgoingCallFragment onDisconnectedFromUser ");
                Toast.makeText(getActivity(), "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment onDisconnectedTimeoutFromUser");
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment onConnectionFailedWithUser");
        setActionButtonsEnability(false);
    }

    @Override
    public void onError(QBRTCSession session, QBRTCException exeption) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment onError");
        Toast.makeText(getActivity(), "ERROR:" + exeption.getMessage(), Toast.LENGTH_LONG).show();
        setActionButtonsEnability(false);
    }

    /* ==========================   Q-municate original code   ==========================*/


    protected abstract int getContentView();

    public static Bundle generateArguments(User friend,
                                           ConstsCore.CALL_DIRECTION_TYPE type, QBRTCTypes.QBConferenceType callType, String sessionId) {

        Bundle args = new Bundle();
        args.putSerializable(ConstsCore.EXTRA_FRIEND, friend);
        args.putSerializable(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, type);
        args.putSerializable(ConstsCore.CALL_TYPE_EXTRA, callType);
        args.putString(ConstsCore.SESSION_ID, sessionId);
        return args;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            Log.d(CALL_INTEGRATION, "OutgoingCallFragment. onAttach");
            outgoingCallFragmentInterface = (OutgoingCallFragmentInterface) activity;
            videoChatHelper = ((CallActivity) getActivity()).getVideoChatHelper();
        } catch (ClassCastException e) {
            ErrorUtils.logError(TAG, e);
        }

        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);

        audioStreamReceiver = new AudioStreamReceiver();
        getActivity().registerReceiver(audioStreamReceiver, intentFilter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callingtime1=stopTimer();
        callingtime=(callingtime1>0)?(int)Math.ceil(callingtime1/60)+1:0;
        if(CallActivity.callflag==1) {
            new ApiRequestTask().execute(String.valueOf(callingtime));
            CallActivity.callflag=0;
        }
        outgoingCallFragmentInterface = null;
        getActivity().unregisterReceiver(audioStreamReceiver);    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
//        if (!callIsStarted) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment. onStart");
        QBRTCClient.getInstance().addConnectionCallbacksListener(this);
//        }
//
//        Log.d(CALL_INTEGRATION, "OutgoingCallFragment. getArguments " + getArguments());
//        if (getArguments() != null) {
//            ConstsCore.CALL_DIRECTION_TYPE directionType = (ConstsCore.CALL_DIRECTION_TYPE) getArguments().getSerializable(ConstsCore.CALL_DIRECTION_TYPE_EXTRA);
//            if (directionType == ConstsCore.CALL_DIRECTION_TYPE.OUTGOING && !callIsStarted) {
//                Log.d(CALL_INTEGRATION, "OutgoingCallFragment. Start call");
//
//                //TODO why we call this metho here
//                ((CallActivity) getActivity()).startCall();
//                callIsStarted = true;
//            }
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        QBRTCClient.getInstance().removeConnectionCallbacksListener(this);
//        QBRTCClient.getInstance().removeConnectionCallbacksListener(OutgoingCallFragment.this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Log.d(CALL_INTEGRATION, "OutgoingCallFragment. onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment. onCreateView ");
        View rootView = inflater.inflate(getContentView(), container, false);
        networkUntil = new NetworkUntil();

        initChatData();
        initUI(rootView);
        return rootView;
    }

    private void initChatData() {

        Log.d(CALL_INTEGRATION, "OutgoingCallFragment. initChatData()");

        if (call_direction_type != null) {
            return;
        }
        call_direction_type = (ConstsCore.CALL_DIRECTION_TYPE) getArguments().getSerializable(
                ConstsCore.CALL_DIRECTION_TYPE_EXTRA);
        opponent = (User) getArguments().getSerializable(ConstsCore.EXTRA_FRIEND);
        opponentcallingid=opponent.getUserId();
        //friend = (User) getArguments().getSerializable(ConstsCore.EXTRA_FRIEND);
        //opponent= UsersDatabaseManager.getUserById(getActivity(), incominguserid);
        call_type = (QBRTCTypes.QBConferenceType) getArguments().getSerializable(
                ConstsCore.CALL_TYPE_EXTRA);
        sessionId = getArguments().getString(ConstsCore.SESSION_ID, "");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.muteDynamicButton:
                switchAudioOutput();
                break;
            case R.id.stopСallButton:
                setActionButtonsEnability(false);
                stopСallButton.setEnabled(false);
                stopСallButton.setActivated(false);
                stopCall();
                Log.d("Track", "Call is stopped");
                break;
            case R.id.muteMicrophoneButton:
                toggleMicrophone();
                break;
            default:
                break;
        }
    }


    private void switchAudioOutput() {
        if (outgoingCallFragmentInterface != null) {
            outgoingCallFragmentInterface.switchSpeaker();
            Log.d(TAG, "Speaker switched!");
        }
    }

    private void toggleMicrophone() {
        if (outgoingCallFragmentInterface != null) {
            if (isAudioEnabled) {
                outgoingCallFragmentInterface.offMic();
                isAudioEnabled = false;
                Log.d(TAG, "Mic is off!");
            } else {
                outgoingCallFragmentInterface.onMic();
                isAudioEnabled = true;
                Log.d(TAG, "Mic is on!");
            }
        }
    }

    public void stopCall() {
        if (outgoingCallFragmentInterface != null) {
            outgoingCallFragmentInterface.hungUpClick();
        }
        //stopTimer();
    }

    private void startTimer(TextView textView) {
        if (handler == null) {
            handler = new Handler();
            updater = new TimeUpdater(textView, handler);
            handler.postDelayed(updater, ConstsCore.SECOND);
        }
    }

    private int stopTimer() {
        if (handler != null && updater != null) {
            //Log.d("myendtime",String.valueOf(updater.totaltime()));
            localcallingtime=updater.totaltime();
            handler.removeCallbacks(updater);
        }
        return localcallingtime;
    }

    private void setDynamicButtonState() {
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.isBluetoothA2dpOn()) {
            // через Bluetooth
            muteDynamicButton.setChecked(true);
        } else if (audioManager.isSpeakerphoneOn()) {
            // через динамик телефона
            muteDynamicButton.setChecked(false);
        } else if (audioManager.isWiredHeadsetOn()) {
            // через проводные наушники
            muteDynamicButton.setChecked(true);
        } else {
            muteDynamicButton.setChecked(false);
        }
    }
    public class ApiRequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }

            String resultstring = null;

            try {
                // url request
                String URL = Config.makeUrl(user.getCoreUrl(), null, false);
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                pairs.add(new BasicNameValuePair("method", "accountapi.callComplete"));
                pairs.add(new BasicNameValuePair("total_time", params[0]));
                pairs.add(new BasicNameValuePair("calltype", CallActivity.calltypeflag));

                // request GET method to server

                resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
            } catch (Exception ex) {
                //mLoading.setVisibility(View.GONE);
                ex.printStackTrace();
                return null;
            }

            return resultstring;
        }

        @Override
        protected void onPostExecute(String result) {
            callComplete(result);
            //mLoading.setVisibility(View.GONE);
            //Toast.makeText(getActivity().getApplicationContext(), user.getTokenkey() , Toast.LENGTH_LONG).show();
            //parsePaymentComplete(result);
        }
    }
    public void callComplete(String resString) {
        if (resString != null) {
            try {
                JSONObject mainJSON = new JSONObject(resString).getJSONObject("output");

                if (mainJSON.has("error_message")) {
                    //Toast.makeText(getActivity().getApplicationContext(), Html.fromHtml(mainJSON.getString("error_message")).toString(), Toast.LENGTH_LONG).show();
                    Log.d("errormessage","errormessage");
                } else {
                    //Toast.makeText(getActivity().getApplicationContext(), Html.fromHtml(mainJSON.getString("message")).toString(), Toast.LENGTH_LONG).show();
                    new UpdateUserInfoTask().execute();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Membership update failed. Please try again." , Toast.LENGTH_LONG).show();
        }
    }
    private class UpdateUserInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }

            String resultstring = null;
            try {
                // Use BasicNameValuePair to create GET data
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                pairs.add(new BasicNameValuePair("method", "accountapi.getUserInfo"));
                pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
                pairs.add(new BasicNameValuePair("login", "1"));

                // url request
                String URL = null;
                if (Config.CORE_URL == null) {
                    URL = Config.makeUrl(user.getCoreUrl(), null, false);
                } else {
                    URL = Config.makeUrl(Config.CORE_URL, null, false);
                }

                // request GET method to server
                resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return resultstring;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(String result) {
            //mLoading.setVisibility(View.GONE);

            if (result == null)
                return;

            try {
                JSONObject mainJson = new JSONObject(result);
                JSONObject outputJson = mainJson.getJSONObject("output");
                if (outputJson.has("total_credit")) {
                    if(Integer.valueOf(outputJson.getString("total_credit"))<=0){
                        user.setCredits(0);
                    }else{
                        user.setCredits(Integer.valueOf(outputJson.getString("total_credit")));
                    }
                    BROADCAST.sideBarFragment.sa.notifyDataSetChanged();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.onPostExecute(result);
        }
    }
    public QBVideoChatHelper getVideoChatHelper() {
        return videoChatHelper;
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }

            if (intent.getIntExtra("state", -1) == 0 /*|| intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -1) == 0*/) {
                muteDynamicButton.setChecked(false);
            } else if (intent.getIntExtra("state", -1) == 1) {
                muteDynamicButton.setChecked(true);
            } else {
//                Toast.makeText(context, "Output audio stream is incorrect", Toast.LENGTH_LONG).show();
            }
            muteDynamicButton.invalidate();


//            Toast.makeText(context, "Audio stream changed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

//    public void registerBroadcastReceiver() {
//        getActivity().registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
//    }
//
//    public void unregisterBroadcastReceiver() {
//        getActivity().unregisterReceiver(myNoisyAudioStreamReceiver);
//    }

    //    private void cancelCallTimer() {
//        if (callTimer != null) {
//            callTimer.cancel();
//            callTimer = null;
//        }
//    }

//    class CancelCallTimerTask extends TimerTask {
//
//        @Override
//        public void run() {
//            if (isExistActivity()) {
//                getBaseActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        stopCall(true, STOP_TYPE.CLOSED);
//                    }
//                });
//            }
//        }
//    }
}