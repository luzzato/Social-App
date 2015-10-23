package com.brodev.socialapp.view.chats;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.brodev.socialapp.dialogs.ConfirmDialog;
import com.brodev.socialapp.utils.Consts;
import com.brodev.socialapp.utils.ImageUtils;
import com.brodev.socialapp.utils.ReceiveFileFromBitmapTask;
import com.brodev.socialapp.utils.ReceiveUriScaledBitmapTask;
import com.brodev.socialapp.view.base.BaseLogeableActivity;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.brodev.socialapp.view.uihelper.SimpleActionModeCallback;
import com.brodev.socialapp.view.uihelper.SimpleTextWatcher;
import com.mypinkpal.app.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.model.QBFile;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.GroupDialog;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBLeaveGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.QBUpdateGroupDialogCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;

import java.io.File;

//import com.quickblox.q_municate_core.db.DatabaseManager;

public class GroupDialogDetailsActivity extends BaseLogeableActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener, AdapterView.OnItemClickListener, ReceiveUriScaledBitmapTask.ReceiveUriScaledBitmapListener {

    public static final int UPDATE_DIALOG_REQUEST_CODE = 100;
    public static final int RESULT_LEAVE_GROUP = 2;

    private EditText groupNameEditText;
    private TextView participantsTextView;
    private TextView participants_textview_online;
    private ListView friendsListView;
    private TextView onlineParticipantsTextView;
    private RoundedImageView avatarImageView;

    private String dialogId;
    private GroupDialog groupDialog;

    private Object actionMode;
    private boolean closeActionMode;
    private boolean isNeedUpdateAvatar;
    private Uri outputUri;

    private Bitmap avatarBitmapCurrent;
    private QBDialog dialogCurrent;
    private String groupNameCurrent;
    private QBChatService chatService;

    private String photoUrlOld;
    private String groupNameOld;
    private int countonline=0;

    private ImageUtils imageUtils;
    private GroupDialogOccupantsAdapter groupDialogOccupantsAdapter;

    public static void start(Activity context, String dialogId) {
        Intent intent = new Intent(context, GroupDialogDetailsActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        context.startActivityForResult(intent, UPDATE_DIALOG_REQUEST_CODE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_dialog_details);
        dialogId = (String) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG_ID);
        dialogCurrent = ChatDatabaseManager.getDialogByDialogId(this, dialogId);
        groupDialog = new GroupDialog(dialogCurrent);
        imageUtils = new ImageUtils(this);
        initUI();
        initUIWithData();
        addActions();
        startLoadGroupDialog();
    }

    @Override
    public void onUriScaledBitmapReceived(Uri originalUri) {
        Log.d("imageurlicon", String.valueOf(originalUri));
        hideProgress();
        startCropActivity(originalUri);
        //Bitmap bitmap = imageUtils.getBitmap(originalUri);
       // new ReceiveFileFromBitmapTask(GroupDialogDetailsActivity.this).execute(imageUtils, bitmap, true);

    }

    private void startLoadGroupDialog() {
        if (dialogCurrent.getType() == QBDialogType.PUBLIC_GROUP)
            initListView();
        else
            QBLoadGroupDialogCommand.start(this, dialogCurrent);
    }

    public void changeAvatarOnClick(View view) {
        canPerformLogout.set(false);
        imageUtils.getImage();
    }

    private void initUI() {
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2b2b2b")));
        actionBar.setTitle("Group Details");

        avatarImageView = _findViewById(R.id.avatar_imageview);
        groupNameEditText = _findViewById(R.id.name_textview);
        participantsTextView = _findViewById(R.id.participants_textview);
        participants_textview_online=_findViewById(R.id.participants_textview_online);
        friendsListView = _findViewById(R.id.chat_friends_listview);
//        onlineParticipantsTextView = _findViewById(R.id.online_participants_textview);
    }

    private void initUIWithData() {
        groupNameEditText.setText(groupDialog.getName());
        participantsTextView.setText(getString(R.string.gdd_participants, groupDialog.getOccupantsCount()));
        //SharedPreferences pref = getSharedPreferences("online_number", Context.MODE_PRIVATE);
        //int total_online = pref.getInt("totalnum", 0);
        participants_textview_online.setText(String.valueOf(groupDialog.getOnlineOccupantsCount()) + "/" + String.valueOf(groupDialog.getOccupantsCount()) + " online");
        /*if(groupDialog.getOccupantsCount()!=0) {
            participants_textview_online.setText(String.valueOf(countonline) + "/" + String.valueOf(groupDialog.getOccupantsCount()) + " online");
        }else if(groupDialog.getOccupantsCount()==0){
            participants_textview_online.setText(String.valueOf(0) + "/" + String.valueOf(groupDialog.getOccupantsCount()) + " online");
        }*/
//        onlineParticipantsTextView.setText(getString(R.string.gdd_online_participants,
//                groupDialog.getOnlineOccupantsCount(), groupDialog.getOccupantsCount()));
        if (!isNeedUpdateAvatar) {
            loadAvatar(groupDialog.getPhotoUrl());
        }
        updateOldGroupData();
    }

    private void loadAvatar(String photoUrl) {
        ImageLoader.getInstance().displayImage(photoUrl, avatarImageView,
                Consts.UIL_GROUP_AVATAR_DISPLAY_OPTIONS);
    }

    private void initListView() {
        groupDialogOccupantsAdapter = getFriendsAdapter();
        friendsListView.setAdapter(groupDialogOccupantsAdapter);
        friendsListView.setOnItemClickListener(this);
    }

    private void addActions() {
        UpdateGroupFailAction updateGroupFailAction = new UpdateGroupFailAction();
        addAction(QBServiceConsts.LOAD_GROUP_DIALOG_SUCCESS_ACTION, new LoadGroupDialogSuccessAction());
        addAction(QBServiceConsts.LOAD_GROUP_DIALOG_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LEAVE_GROUP_DIALOG_SUCCESS_ACTION, new LeaveGroupDialogSuccessAction());
        addAction(QBServiceConsts.LEAVE_GROUP_DIALOG_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.UPDATE_GROUP_DIALOG_SUCCESS_ACTION, new UpdateGroupNameSuccessAction());
        addAction(QBServiceConsts.UPDATE_GROUP_DIALOG_FAIL_ACTION, updateGroupFailAction);
        addAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, new UpdateGroupPhotoSuccessAction());
        addAction(QBServiceConsts.UPDATE_USER_FAIL_ACTION, updateGroupFailAction);
        updateBroadcastActionList();
    }

    protected GroupDialogOccupantsAdapter getFriendsAdapter() {
        return new GroupDialogOccupantsAdapter(this, groupDialog.getOccupantList());
        /*if (dialogCurrent.getType() == QBDialogType.PUBLIC_GROUP)
            return new GroupDialogOccupantsAdapter(this, ChatUtils.getJoinUsers());
        else
            return new GroupDialogOccupantsAdapter(this, groupDialog.getOccupantList());*/
    }

    private void showLeaveGroupDialog() {
        ConfirmDialog dialog = ConfirmDialog.newInstance(R.string.dlg_leave_group, R.string.dlg_confirm);
        dialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress();
                QBLeaveGroupDialogCommand.start(GroupDialogDetailsActivity.this, groupDialog.getRoomJid());
                navigateToParent();
                GroupDialogActivity.fa.finish();
            }
        });
        dialog.show(getFragmentManager(), null);
    }

    private void initTextChangedListeners() {
        groupNameEditText.addTextChangedListener(new GroupNameTextWatcherListener());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            groupNameEditText.setText(groupDialog.getName());
            closeActionMode = true;
            ((ActionMode) actionMode).finish();
            return true;
        } else {
            closeActionMode = false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
     public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        getSupportMenuInflater().inflate(R.menu.group_dialog_details_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            case R.id.action_add:
                startAddFriendsActivity();
                return true;
            case R.id.action_leave:
                showLeaveGroupDialog();
                return true;
        }
        return false;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("request/result",String.valueOf(requestCode)+"/"+String.valueOf(resultCode));
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        } else  if (requestCode == ImageUtils.GALLERY_INTENT_CALLED && resultCode == RESULT_OK) {
            Uri originalUri = data.getData();
            if (originalUri != null) {
                showProgress();
                new ReceiveUriScaledBitmapTask(this).execute(imageUtils, originalUri);
            }
        }
        canPerformLogout.set(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startAddFriendsActivity() {
        AddFriendsToGroupActivity.start(this, groupDialog);
        /* bronislaw
        int countUnselectedFriendsInChat = DatabaseManager.getFriendsFilteredByIds(this,
                FriendUtils.getFriendIds(groupDialog.getOccupantList())).getCount();
        if (countUnselectedFriendsInChat != ConstsCore.ZERO_INT_VALUE) {
            AddFriendsToGroupActivity.start(this, groupDialog);
        } else {
            DialogUtils.showLong(this, getResources().getString(R.string.gdd_all_friends_is_in_group));
        }
        */
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateAvatar = true;
            avatarBitmapCurrent = imageUtils.getBitmap(outputUri);
            avatarImageView.setImageBitmap(avatarBitmapCurrent);
            startAction();
        }else if (resultCode == Crop.RESULT_ERROR) {
            DialogUtils.showLong(this, Crop.getError(result).getMessage());
        }
    }

    private void startCropActivity(Uri originalUri) {

        outputUri = Uri.fromFile(new File(getCacheDir(), Crop.class.getName()));
        new Crop(originalUri).output(outputUri).asSquare().start(this);

    }

    private void startAction() {
        if (actionMode != null) {
            return;
        }
        actionMode = startActionMode(new ActionModeCallback());
    }

    private void updateCurrentUserData() {
        groupNameCurrent = groupNameEditText.getText().toString();
    }

    private void updateUserData() {
        updateCurrentUserData();
        if (isGroupDataChanged()) {
            saveChanges();
        }
    }

    private boolean isGroupDataChanged() {
        return !groupNameCurrent.equals(groupNameOld) || isNeedUpdateAvatar;
    }

    private void saveChanges() {
        if (!isUserDataCorrect()) {
            DialogUtils.showLong(this, getString(R.string.gdd_name_not_entered));
            return;
        }

        dialogCurrent.setName(groupNameCurrent);
        if (isNeedUpdateAvatar) {
            new ReceiveFileFromBitmapTask(this).execute(imageUtils, avatarBitmapCurrent, true);
        } else {
            startUpdatingGroupDialog(null);
        }
        showProgress();
    }

    private boolean isUserDataCorrect() {
        return !TextUtils.isEmpty(groupNameCurrent);
    }

    private void updateOldGroupData() {
        groupNameOld = groupDialog.getName();
        photoUrlOld = groupDialog.getPhotoUrl();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        User selectedFriend = groupDialogOccupantsAdapter.getItem(position);
        if (selectedFriend != null) {
            startFriendProfile(selectedFriend);
        }
    }

    private void startFriendProfile(User selectedFriend) {
        QBUser currentUser = AppSession.getSession().getUser();
        /*
        if (currentUser.getId() == selectedFriend.getUserId()) {
            ProfileActivity.start(GroupDialogDetailsActivity.this);
        } else {
            FriendDetailsActivity.start(GroupDialogDetailsActivity.this, selectedFriend.getUserId());
        }
        */
    }

    private void resetGroupData() {
        groupNameEditText.setText(groupNameOld);
        isNeedUpdateAvatar = false;
        loadAvatar(photoUrlOld);
    }

    private void startUpdatingGroupDialog(File imageFile) {
        //showProgress();
        QBUpdateGroupDialogCommand.start(this, dialogCurrent, imageFile);
    }

    @Override
    public void onCachedImageFileReceived(File imageFile) {
        Log.d("imagefileurl","imagefileurl");
        startUpdatingGroupDialog(imageFile);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
        Log.d("imagefileurl",absolutePath);
    }

    private class GroupNameTextWatcherListener extends SimpleTextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!groupNameOld.equals(s.toString())) {
                startAction();
            }
        }
    }

    private class ActionModeCallback extends SimpleActionModeCallback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (!closeActionMode) {
                updateUserData();
            }
            actionMode = null;
        }
    }

    private class LoadGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            groupDialog = (GroupDialog) bundle.getSerializable(QBServiceConsts.EXTRA_GROUP_DIALOG);
            updateOldGroupData();
            initUIWithData();
            initTextChangedListeners();
            initListView();
            hideProgress();
        }
    }

    private class UpdateGroupFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            DialogUtils.showLong(GroupDialogDetailsActivity.this, exception.getMessage());
            resetGroupData();
            hideProgress();
        }
    }

    private class LeaveGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            setResult(RESULT_LEAVE_GROUP, getIntent());
            finish();
        }
    }

    private class UpdateGroupNameSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            updateOldGroupData();
            hideProgress();
        }
    }

    private class UpdateGroupPhotoSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBDialog dialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            groupDialog = new GroupDialog(ChatDatabaseManager.getDialogByDialogId(GroupDialogDetailsActivity.this,
                    dialog.getDialogId()));
            updateOldGroupData();
            hideProgress();
        }
    }
}