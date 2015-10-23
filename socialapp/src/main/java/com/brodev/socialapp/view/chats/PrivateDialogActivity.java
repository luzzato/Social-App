package com.brodev.socialapp.view.chats;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.dialogs.AlertDialog;
import com.brodev.socialapp.http.NetworkUntil;
import com.brodev.socialapp.utils.ReceiveFileFromBitmapTask;
import com.brodev.socialapp.view.FriendTabsPager;
import com.brodev.socialapp.view.mediacall.CallActivity;
import com.jirbo.adcolony.*;
import com.mypinkpal.app.R;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.db.tables.FriendTable;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBAcceptFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBRejectFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBUpdateDialogCommand;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

//import com.quickblox.q_municate_core.db.DatabaseManager;

public class PrivateDialogActivity extends BaseDialogActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener,AdColonyAdListener, AdColonyAdAvailabilityListener {

    private ContentObserver statusContentObserver;
    private ContentObserver friendsTableContentObserver;
    private Cursor friendCursor;
    private FriendOperationAction friendOperationAction;
    private com.brodev.socialapp.entity.User user;
    final static String APP_ID  = "appcab9c2776f9549b88f";
    final static String ZONE_ID = "vzb805f3de0b244f8b8f";
    private boolean available=false;
    private boolean adviewingend=false;
    private String phonecalltype="";
    private SharedPreferences prefs;

    public PrivateDialogActivity() {
        super(R.layout.activity_dialog, QBService.PRIVATE_CHAT_HELPER);
    }

    public static void start(Context context, User opponent, QBDialog dialog) {
        Intent intent = new Intent(context, PrivateDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENT, opponent);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AdColony.configure(this, "version:1.0,store:google", APP_ID, ZONE_ID);
        AdColony.addAdAvailabilityListener(this);

        // Disable rotation if not on a tablet-sized device (note: not
        // necessary to use AdColony).
        try {
            if (!AdColony.isTablet()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }catch (Exception ex){
            Log.d("myadcolnonyerror","myadcolnonyerror");
        }
        prefs = this.getSharedPreferences("mypinkpal_user", Context.MODE_PRIVATE);
        friendOperationAction = new FriendOperationAction();
        opponentFriend = (User) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_OPPONENT);
        dialog = (QBDialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);

        dialogId = dialog.getDialogId();
        friendCursor = UsersDatabaseManager.getFriendCursorById(this, opponentFriend.getUserId());
        initListView();
        initActionBar();
        registerContentObservers();
        setCurrentDialog(dialog);

        // Register Global Dialog ID
        QBUser me = AppSession.getSession().getUser();
        ChatUtils.createOccupantsIdsFromPrivateMessage(me.getId(), opponentFriend.getUserId());
        /*ChatUtils.joinDialogId = "";
        ChatUtils.joinDialogId = dialog.getDialogId();
        ChatUtils.joinUserLists.clear();*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        Crouton.cancelAllCroutons();
        AdColony.pause();
    }

    @Override
    protected void onUpdateChatDialog() {
        if (!messagesAdapter.isEmpty()) {
            startUpdateChatDialog();
        }
    }

    @Override
    protected void onDestroy() {
        /*try {
            ((QBPrivateChatHelper) chatHelper).sendOccupantStatusPrivate("leave", opponentFriend.getUserId());
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }*/
        try {
//            ((QBPrivateChatHelper) chatHelper).sendOccupantStatusPrivate("leave", opponentFriend.getUserId());
            ((QBPrivateChatHelper) chatHelper).closeChat(getDialog(), null);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }

        super.onDestroy();
        currentOpponent = null;
        unregisterStatusChangingObserver();

        // Register Global Dialog ID
        /*ChatUtils.joinDialogId = "";
        ChatUtils.joinUserLists.clear();*/
    }

    @Override
    protected void onFileSelected(Uri originalUri) {
        Bitmap bitmap = imageUtils.getBitmap(originalUri);
        new ReceiveFileFromBitmapTask(PrivateDialogActivity.this).execute(imageUtils, bitmap, true);
    }

    @Override
    protected void onFileLoaded(QBFile file) {
        try {
            ((QBPrivateChatHelper) chatHelper).sendPrivateMessageWithAttachImage(file,
                    opponentFriend.getUserId());
        } catch (QBResponseException exc) {
            ErrorUtils.showError(this, exc);
        }
        scrollListView();
    }

    @Override
    protected Bundle generateBundleToInitDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, opponentFriend.getUserId());
        return bundle;
    }

    private void registerContentObservers() {
        statusContentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                User user = opponentFriend;
                opponentFriend = UsersDatabaseManager.getUserById(PrivateDialogActivity.this,
                        PrivateDialogActivity.this.opponentFriend.getUserId());
                if (opponentFriend != null)
                    setOnlineStatus(opponentFriend);
                else {
//                    actionBar.setSubtitle("Offline");
                    opponentFriend = user;
                }
            }
        };
        friendCursor.registerContentObserver(statusContentObserver);

        friendsTableContentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                checkMessageSendingPossibility();
            }
        };
        getContentResolver().registerContentObserver(FriendTable.CONTENT_URI, true, friendsTableContentObserver);
    }

    private void unregisterStatusChangingObserver() {
        if (friendCursor != null && statusContentObserver != null) {
            friendCursor.unregisterContentObserver(statusContentObserver);
        }

        if (friendsTableContentObserver != null) {
            getContentResolver().unregisterContentObserver(friendsTableContentObserver);
        }
    }

    private void setOnlineStatus(User friend) {
        /*
        ActionBar actionBar = getActionBar();
        try {
            actionBar.setSubtitle(friend.getOnlineStatus(this));
        } catch (Exception e) {
            System.out.println("Exception :" + e);
        }
        */
    }

    private void startUpdateChatDialog() {
        if (dialog != null) {
            QBUpdateDialogCommand.start(this, getDialog());
        }
    }

    private QBDialog getDialog() {
        Cursor cursor = (Cursor) messagesAdapter.getItem(messagesAdapter.getCount() - 1);

        MessageCache messageCache = ChatDatabaseManager.getMessageCacheFromCursor(cursor);
        if (messageCache.getMessagesNotificationType() == null) {
            dialog.setLastMessage(messageCache.getMessage());
        } else {
            dialog.setLastMessage(getResources().getString(R.string.frl_friends_contact_request));
        }

        dialog.setLastMessageDateSent(messageCache.getTime());
        dialog.setUnreadMessageCount(ConstsCore.ZERO_INT_VALUE);
        dialog.setLastMessageUserId(messageCache.getSenderId());
        dialog.setType(QBDialogType.PRIVATE);
        return dialog;
    }

    private void initListView() {
        messagesAdapter = new PrivateDialogMessagesAdapter(this, friendOperationAction,
                getAllDialogMessagesByDialogId(), this, dialog);
        messagesListView.setAdapter((StickyListHeadersAdapter) messagesAdapter);
        ((PrivateDialogMessagesAdapter) messagesAdapter).findLastFriendsRequestMessagesPosition();
    }

    private void initActionBar() {
        actionBar.setTitle(opponentFriend.getFullName());
//        actionBar.setSubtitle(opponentFriend.getOnlineStatus(this));
//        actionBar.setLogo(R.drawable.placeholder_user);
        actionBar.setDisplayShowHomeEnabled(false);
        if (!TextUtils.isEmpty(opponentFriend.getAvatarUrl())) {
            loadLogoActionBar(opponentFriend.getAvatarUrl());
        }
    }

    @Override
    public void onCachedImageFileReceived(File file) {
        startLoadAttachFile(file);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    public void sendMessageOnClick(View view) {
        Log.d("notifymessagefriend",String.valueOf(opponentFriend.getUserId())+"/"+String.valueOf(opponentFriend.isOnline()));
        if ((ChatUtils.getOccupantsIdsListForCreatePrivateDialog(opponentFriend.getUserId()).size() == 0)||(opponentFriend.isOnline()==false)) {
            SendGCMMessage sendGCM = new SendGCMMessage(this);
            sendGCM.execute();
        }

        try {
            ((QBPrivateChatHelper) chatHelper).sendPrivateMessage(messageEditText.getText().toString(),
                    opponentFriend.getUserId());
        } catch (QBResponseException exc) {
            ErrorUtils.showError(this, exc);
        }

        messageEditText.setText(ConstsCore.EMPTY_STRING);
        isNeedToScrollMessages = true;
        scrollListView();
    }

    @Override
    public void onAdColonyAdAvailabilityChange(boolean b, String s) {
        if(b)available=b;
    }

    @Override
    public void onAdColonyAdAttemptFinished(AdColonyAd adColonyAd) {
        Log.d("AdColony", "onAdColonyAdAttemptFinished");
      if(phonecalltype.equals("action_audio_call")) {
            callToUser(opponentFriend, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO);
        }else if(phonecalltype.equals("action_video_call")){
            callToUser(opponentFriend, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
        }
    }

    @Override
    public void onAdColonyAdStarted(AdColonyAd adColonyAd) {
        Log.d("AdColony", "onAdColonyAdStarted");
    }

    public class SendGCMMessage extends AsyncTask<String, Void, String> {
        private NetworkUntil network = new NetworkUntil();

        public SendGCMMessage(Context context) {
            user = (com.brodev.socialapp.entity.User) getApplicationContext();
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = null;
            NetworkUntil network = new NetworkUntil();

            if (Config.CORE_URL == null) {
                URL = Config.makeUrl(user.getCoreUrl(), null, false);
            } else {
                URL = Config.makeUrl(Config.CORE_URL, null, false);
            }

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
            pairs.add(new BasicNameValuePair("method", "accountapi.notifyMsgToOfflineUser"));
            pairs.add(new BasicNameValuePair("qb_user_id", String.valueOf(opponentFriend.getUserId())));
            pairs.add(new BasicNameValuePair("dialog_id", dialog.getDialogId()));

            String result = network.makeHttpRequest(URL, "POST", pairs);
            return result;
        }
    }

    public class SendGCMCall extends AsyncTask<String, Void, String> {
        private NetworkUntil network = new NetworkUntil();

        public SendGCMCall(Context context) {
            user = (com.brodev.socialapp.entity.User) getApplicationContext();
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = null;
            NetworkUntil network = new NetworkUntil();

            if (Config.CORE_URL == null) {
                URL = Config.makeUrl(user.getCoreUrl(), null, false);
            } else {
                URL = Config.makeUrl(Config.CORE_URL, null, false);
            }

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
            pairs.add(new BasicNameValuePair("method", "accountapi.notifyCallToOfflineUser"));
            pairs.add(new BasicNameValuePair("qb_user_id", String.valueOf(opponentFriend.getUserId())));
            pairs.add(new BasicNameValuePair("dialog_id", dialog.getDialogId()));

            String result = network.makeHttpRequest(URL, "POST", pairs);
            return result;
        }
    }
    public class SendGCMVideoCall extends AsyncTask<String, Void, String> {
        private NetworkUntil network = new NetworkUntil();

        public SendGCMVideoCall(Context context) {
            user = (com.brodev.socialapp.entity.User) getApplicationContext();
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = null;
            NetworkUntil network = new NetworkUntil();

            if (Config.CORE_URL == null) {
                URL = Config.makeUrl(user.getCoreUrl(), null, false);
            } else {
                URL = Config.makeUrl(Config.CORE_URL, null, false);
            }

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
            pairs.add(new BasicNameValuePair("method", "accountapi.notifyVideoCallToOfflineUser"));
            pairs.add(new BasicNameValuePair("qb_user_id", String.valueOf(opponentFriend.getUserId())));
            pairs.add(new BasicNameValuePair("dialog_id", dialog.getDialogId()));

            String result = network.makeHttpRequest(URL, "POST", pairs);
            return result;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.private_dialog_menu, menu);

        /*try {
            ((QBPrivateChatHelper) chatHelper).sendOccupantStatusPrivate("join", opponentFriend.getUserId());
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }*/
        try {
//            ((QBPrivateChatHelper) chatHelper).sendOccupantStatusPrivate("join", opponentFriend.getUserId());
            ((QBPrivateChatHelper) chatHelper).createPrivateDialogIfNotExist(opponentFriend.getUserId());
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        boolean isFriend = DatabaseManager.isFriendInBase(PrivateDialogActivity.this, opponentFriend.getUserId());
        boolean isFriend = true;
        if (!isFriend && item.getItemId() != android.R.id.home) {
            DialogUtils.showLong(PrivateDialogActivity.this, getResources().getString(R.string.dlg_user_is_not_friend));
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d("mytitle","mytitle");
                navigateToParent();
                return true;
            case R.id.action_attach:
                attachButtonOnClick();
                return true;
            case R.id.action_audio_call:
                phonecalltype="action_audio_call";
                if(prefs.getInt("total_credit",0)<=0){
                    Toast.makeText(this, "Please purchase credits or upgrade your membership", Toast.LENGTH_LONG).show();
                }else {
                    AdColonyVideoAd ad = new AdColonyVideoAd(ZONE_ID).withListener(PrivateDialogActivity.this);
                    ad.show();
                }
                return true;
            case R.id.action_video_call:
                phonecalltype="action_video_call";
                if(prefs.getInt("total_credit",0)<=0){
                    Toast.makeText(this, "Please purchase credits or upgrade your membership", Toast.LENGTH_LONG).show();
                }else {
                    AdColonyVideoAd ad1 = new AdColonyVideoAd(ZONE_ID).withListener(PrivateDialogActivity.this);
                    ad1.show();
                }
                return true;
            case R.id.action_view_profile:
                Intent intent = new Intent(this, FriendTabsPager.class);
                intent.putExtra("user_id", opponentFriend.getExternalId());
                startActivity(intent);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    private void callToUser(User friend, final QBRTCTypes.QBConferenceType callType) {
        Log.d("notifymessagefriend",String.valueOf(opponentFriend.getUserId())+"/"+String.valueOf(opponentFriend.isOnline()));
        if (friend.getUserId() != AppSession.getSession().getUser().getId()) {
            if ((ChatUtils.getOccupantsIdsListForCreatePrivateDialog(opponentFriend.getUserId()).size() == 0)||(opponentFriend.isOnline()==false)) {
                //SendGCMMessage sendGCM = new SendGCMMessage(this);
                //sendGCM.execute();
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                audiocall(callType);
                            }
                        },
                        20000);
                /*if(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO.equals(callType)){
                    SendGCMCall sendGCM = new SendGCMCall(this);
                    sendGCM.execute();
                }else {
                    SendGCMVideoCall sendGCM = new SendGCMVideoCall(this);
                    sendGCM.execute();
                }*/

            }
            CallActivity.start(PrivateDialogActivity.this, friend, callType);
        }
    }
    private void audiocall(QBRTCTypes.QBConferenceType callType){
        if(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO.equals(callType)){
            SendGCMCall sendGCM = new SendGCMCall(this);
            sendGCM.execute();
        }else {
            SendGCMVideoCall sendGCM = new SendGCMVideoCall(this);
            sendGCM.execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdColony.resume(this);
        scrollListView();
        startLoadDialogMessages();
        currentOpponent = opponentFriend.getFullName();

        checkMessageSendingPossibility();

    }

    private void checkMessageSendingPossibility() {
        boolean isFriend = UsersDatabaseManager.isFriendInBase(PrivateDialogActivity.this, opponentFriend.getUserId());
//        messageEditText.setEnabled(isFriend);
    }

    private void acceptUser(final int userId) {
        showProgress();
        QBAcceptFriendCommand.start(this, userId);
    }

    private void rejectUser(final int userId) {
        showRejectUserDialog(userId);
    }

    private void showRejectUserDialog(final int userId) {
        User user = UsersDatabaseManager.getUserById(this, userId);
        AlertDialog alertDialog = AlertDialog.newInstance(getResources().getString(
                R.string.frl_dlg_reject_friend, user.getFullName()));
        alertDialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress();
                QBRejectFriendCommand.start(PrivateDialogActivity.this, userId);
            }
        });
        alertDialog.setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show(getFragmentManager(), null);
    }

    public interface FriendOperationListener {

        void onAcceptUserClicked(int userId);

        void onRejectUserClicked(int userId);
    }

    private class FriendOperationAction implements FriendOperationListener {

        @Override
        public void onAcceptUserClicked(int userId) {
            acceptUser(userId);
        }

        @Override
        public void onRejectUserClicked(int userId) {
            rejectUser(userId);
        }
    }
}