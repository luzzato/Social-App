package com.brodev.socialapp.view.chats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.brodev.socialapp.utils.ReceiveFileFromBitmapTask;
import com.mypinkpal.app.R;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBUpdateDialogCommand;
import com.quickblox.q_municate_core.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

//import com.quickblox.q_municate_core.db.DatabaseManager;

public class GroupDialogActivity extends BaseDialogActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener {

    private String groupName;
    public static Activity fa;

    public GroupDialogActivity() {
        super(R.layout.activity_dialog, QBService.MULTI_CHAT_HELPER);
    }

    public static void start(Context context, ArrayList<User> friends) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friends);
        context.startActivity(intent);
    }

    public static void start(Context context, QBDialog qbDialog) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID, qbDialog.getDialogId());
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, qbDialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fa=this;

        if (getIntent().hasExtra(QBServiceConsts.EXTRA_ROOM_JID)) {
            dialogId = getIntent().getStringExtra(QBServiceConsts.EXTRA_ROOM_JID);
        }

        dialog = (QBDialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
        initListView();
        startLoadDialogMessages();
        setCurrentDialog(dialog);

        registerForContextMenu(messagesListView);

        // Register Global Dialog ID
        /*ChatUtils.joinDialogId = "";
        ChatUtils.joinDialogId = dialog.getDialogId();
        ChatUtils.joinUserLists.clear();*/
    }

    @Override
    protected void onUpdateChatDialog() {
        if (messagesAdapter != null && !messagesAdapter.isEmpty()) {
            startUpdateChatDialog();
        }
    }

    @Override
    protected void onDestroy() {
        //((QBMultiChatHelper) chatHelper).sendOccupantStatusGroup("leave");
        try {
            ((QBMultiChatHelper) chatHelper).leaveDialogs();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
        removeActions();

        // Register Global Dialog ID
        /*ChatUtils.joinDialogId = "";
        ChatUtils.joinUserLists.clear();*/
    }

    protected void removeActions() {
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION);
    }

    @Override
    protected void onFileSelected(Uri originalUri) {
        Bitmap bitmap = imageUtils.getBitmap(originalUri);
        new ReceiveFileFromBitmapTask(GroupDialogActivity.this).execute(imageUtils, bitmap, true);
    }

    @Override
    protected void onFileLoaded(QBFile file) {
        try {
            ((QBMultiChatHelper) chatHelper).sendGroupMessageWithAttachImage(dialog.getRoomJid(), file);
        } catch (QBResponseException e) {
            ErrorUtils.showError(this, e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GroupDialogDetailsActivity.UPDATE_DIALOG_REQUEST_CODE == requestCode &&
                GroupDialogDetailsActivity.RESULT_LEAVE_GROUP == resultCode) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected Bundle generateBundleToInitDialog() {
        return null;
    }

    private void startUpdateChatDialog() {
        QBDialog dialog = getQBDialog();
        if (dialog != null) {
            QBUpdateDialogCommand.start(this, dialog);
        }
    }

    private QBDialog getQBDialog() {
        Cursor cursor = (Cursor) messagesAdapter.getItem(messagesAdapter.getCount() - 1);

        MessageCache messageCache = ChatDatabaseManager.getMessageCacheFromCursor(cursor);
        if (messageCache.getMessagesNotificationType() == null) {
            dialog.setLastMessage(messageCache.getMessage());
        } else {
            dialog.setLastMessage(getResources().getString(R.string.frl_friends_contact_request));
        }

        dialog.setLastMessageDateSent(messageCache.getTime());
        dialog.setUnreadMessageCount(ConstsCore.ZERO_INT_VALUE);
        return dialog;
    }

    private void updateChatData() {
        dialog = ChatDatabaseManager.getDialogByDialogId(this, dialogId);
        if (dialog != null) {
            groupName = dialog.getName();
            updateActionBar();
        }
    }

    private void initListView() {
        messagesAdapter = new GroupDialogMessagesAdapter(this, getAllDialogMessagesByDialogId(), this,
                dialog);
        messagesListView.setAdapter((StickyListHeadersAdapter) messagesAdapter);
    }

    private void updateActionBar() {
        actionBar.setTitle(groupName);
        actionBar.setSubtitle(getString(R.string.gdd_participants, dialog.getOccupants().size()));
        actionBar.setLogo(R.drawable.placeholder_group);
        if (!TextUtils.isEmpty(dialog.getPhoto())) {
            loadLogoActionBar(dialog.getPhoto());
        }
    }

    @Override
    public void onCachedImageFileReceived(File file) {
        Log.d("cachimageurl", "cachimageurl");
        startLoadAttachFile(file);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    public void sendMessageOnClick(View view) {
        try {
            ((QBMultiChatHelper) chatHelper).sendGroupMessage(dialog.getRoomJid(),
                    messageEditText.getText().toString());
        } catch (QBResponseException e) {
            ErrorUtils.showError(this, e);
        }
        messageEditText.setText(ConstsCore.EMPTY_STRING);
        isNeedToScrollMessages = true;
        scrollListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_dialog_menu, menu);

        dialog = (QBDialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
//        ((QBMultiChatHelper) chatHelper).Join("join");
        try {
            ((QBMultiChatHelper) chatHelper).joinRoomChat(dialog);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (QBResponseException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            case R.id.action_attach:
                attachButtonOnClick();
                return true;
            case R.id.action_group_details:
                GroupDialogDetailsActivity.start(this, dialog.getDialogId());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, view, menuInfo);
//        MenuInflater m = getMenuInflater();
//        m.inflate(R.menu.group_dialog_ctx_menu, menu);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        updateChatData();
        scrollListView();
    }
}