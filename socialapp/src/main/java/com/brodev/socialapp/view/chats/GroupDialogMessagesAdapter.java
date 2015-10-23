package com.brodev.socialapp.view.chats;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.mypinkpal.app.R;
import com.brodev.socialapp.view.chats.emoji.EmojiTextView;
import com.brodev.socialapp.view.imageview.MaskedImageView;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.brodev.socialapp.utils.DateUtils;
//import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.db.tables.MessageTable;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.utils.ConstsCore;

public class GroupDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    public GroupDialogMessagesAdapter(Context context, Cursor cursor,
            ScrollMessagesListener scrollMessagesListener, QBDialog dialog) {
        super(context, cursor);
        this.scrollMessagesListener = scrollMessagesListener;
        this.dialog = dialog;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        ViewHolder viewHolder = new ViewHolder();

        int senderId = cursor.getInt(cursor.getColumnIndex(MessageTable.Cols.SENDER_ID));
        if (isOwnMessage(senderId)) {
            view = layoutInflater.inflate(R.layout.list_item_message_own, null, true);
        } else {
            view = layoutInflater.inflate(R.layout.list_item_group_message_opponent, null, true);
            viewHolder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
            setViewVisibility(viewHolder.avatarImageView, View.VISIBLE);
            viewHolder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
            setViewVisibility(viewHolder.nameTextView, View.VISIBLE);
        }

        viewHolder.attachMessageRelativeLayout = (RelativeLayout) view.findViewById(
                R.id.attach_message_relativelayout);
        viewHolder.timeAttachMessageTextView = (TextView) view.findViewById(
                R.id.time_attach_message_textview);
        viewHolder.progressRelativeLayout = (RelativeLayout) view.findViewById(R.id.progress_relativelayout);
        viewHolder.textMessageView = view.findViewById(R.id.text_message_view);
        viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
        viewHolder.attachImageView = (MaskedImageView) view.findViewById(R.id.attach_imageview);
        viewHolder.timeTextMessageTextView = (TextView) view.findViewById(R.id.time_text_message_textview);
        viewHolder.verticalProgressBar = (ProgressBar) view.findViewById(R.id.vertical_progressbar);
        viewHolder.verticalProgressBar.setProgressDrawable(context.getResources().getDrawable(
                R.drawable.vertical_progressbar));
        viewHolder.centeredProgressBar = (ProgressBar) view.findViewById(R.id.centered_progressbar);

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String avatarUrl = null;
        String senderName;

        MessageCache messageCache = ChatDatabaseManager.getMessageCacheFromCursor(cursor);

        boolean ownMessage = isOwnMessage(messageCache.getSenderId());

        resetUI(viewHolder);

        viewHolder.messageDeliveryStatusImageView = (ImageView) view.findViewById(
                R.id.text_message_delivery_status_imageview);
        viewHolder.messageDeliveryStatusImageView = (ImageView) view.findViewById(
                R.id.attach_message_delivery_status_imageview);

        if (ownMessage) {
            avatarUrl = getAvatarUrlForCurrentUser();
        } else {
            User senderFriend = UsersDatabaseManager.getUserById(context, messageCache.getSenderId());
            if (senderFriend != null) {
                senderName = senderFriend.getFullName();
                SharedPreferences profilepref = context.getSharedPreferences("mypinkpal_friendlist", Context.MODE_PRIVATE);
                avatarUrl = profilepref.getString(senderFriend.getEmail(), "");
                //Log.d("friendavatar",avatar);
                //avatarUrl = getAvatarUrlForFriend(senderFriend);
            } else {
                senderName = messageCache.getSenderId() + ConstsCore.EMPTY_STRING;
            }
            viewHolder.nameTextView.setTextColor(getTextColor(messageCache.getSenderId()));
            viewHolder.nameTextView.setText(senderName);
        }

        if (!TextUtils.isEmpty(messageCache.getAttachUrl())) {
            viewHolder.timeAttachMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
            setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
            displayAttachImage(messageCache.getAttachUrl(), viewHolder);
        } else {
            setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
            viewHolder.messageTextView.setText(messageCache.getMessage());
        }

        if (!messageCache.isRead() && !ownMessage) {
            messageCache.setRead(true);
            QBUpdateStatusMessageCommand.start(context, dialog, messageCache, false);
        }

        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);
    }
}