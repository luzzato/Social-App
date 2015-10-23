package com.brodev.socialapp.view.chats;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brodev.socialapp.entity.Friend;
import com.brodev.socialapp.view.base.BaseCursorAdapter;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.mypinkpal.app.R;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

//import com.quickblox.q_municate_core.db.DatabaseManager;

/**
 * Created by Bebel on 2/4/15.
 */
public class GroupDialogsAdapter extends BaseCursorAdapter {

    public GroupDialogsAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView;
        convertView = layoutInflater.inflate(R.layout.list_item_dialog, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
        viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
        viewHolder.createdTextview = (TextView) convertView.findViewById(R.id.created_name_textview);
       // viewHolder.lastMessageTextView = (TextView) convertView.findViewById(R.id.last_message_textview);


        viewHolder.unreadMessagesTextView = (TextView) convertView.findViewById(
                R.id.unread_messages_textview);
        convertView.setTag(viewHolder);
        return convertView;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        QBDialog dialog = ChatDatabaseManager.getDialogFromCursor(cursor);

        /**
         * Public Chatroom lists
         */
        boolean compareRoomName = false;
        ArrayList<String> fixedChatroomList = new ArrayList<String>();
        fixedChatroomList.add("Lesbian Chat Room");
        fixedChatroomList.add("Gay Chat Room");
        fixedChatroomList.add("Bisexual Chat Room");
        fixedChatroomList.add("Transexual Chat Room");
        fixedChatroomList.add("Transgender Chat Room");
        fixedChatroomList.add("Questioning Chat Room");
        fixedChatroomList.add("LGBT Pride Chat Room");
        fixedChatroomList.add("LGBT Chat Room");
        fixedChatroomList.add("Ethnic Chat Room");
        fixedChatroomList.add("Religious Chat Room");

        /*
        if (dialog.getType().equals(QBDialogType.PRIVATE)) {
            int occupantId = ChatUtils.getOccupantIdFromList(dialog.getOccupants());
            User occupant = getOccupantById(occupantId);
            viewHolder.nameTextView.setText(occupant.getFullName());
            displayAvatarImage(getAvatarUrlForFriend(occupant), viewHolder.avatarImageView);
        } else {
            viewHolder.nameTextView.setText(dialog.getName());
            viewHolder.avatarImageView.setImageResource(R.drawable.placeholder_group);
            displayGroupPhotoImage(dialog.getPhoto(), viewHolder.avatarImageView);
        }
        */

        //int userId=dialog.getUserId();
        if (!dialog.getType().equals(QBDialogType.PRIVATE)) {
            viewHolder.nameTextView.setText(dialog.getName());
            //viewHolder.createdTextview.setText(String.valueOf(dialog.getUserId()));
            viewHolder.avatarImageView.setImageResource(R.drawable.placeholder_group);
            displayGroupPhotoImage(dialog.getPhoto(), viewHolder.avatarImageView);

            if (dialog.getUnreadMessageCount() > ConstsCore.ZERO_INT_VALUE) {
                viewHolder.unreadMessagesTextView.setText(dialog.getUnreadMessageCount() + ConstsCore.EMPTY_STRING);
                viewHolder.unreadMessagesTextView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.unreadMessagesTextView.setVisibility(View.GONE);
            }

            for (int i = 0; i < fixedChatroomList.size(); i++) {
                if (dialog.getName().equals(fixedChatroomList.get(i))) {
                    compareRoomName = true;
                    break;
                }
            }
//            viewHolder.lastMessageTextView.setText(dialog.getLastMessage());
            if (compareRoomName == false) {
                //Log.d("dialogresult",String.valueOf(dialog.getUserId()));
                if ((dialog.getUserId() != null)&&(dialog.getUserId()!=851700)) {
                    //viewHolder.lastMessageTextView.setText(getOccupantById(dialog.getUserId()));
                    viewHolder.createdTextview.setText("Create by: "+getOccupantById(dialog.getUserId()));
                    //viewHolder.createdTextview.setText(String.valueOf(dialog.getUserId()));
                }else if((dialog.getUserId() != null)&&(dialog.getUserId()==851700)){
                    viewHolder.createdTextview.setText("Create by: Mypinkpal.com");
                }
            }else{
                viewHolder.createdTextview.setText("Create by: Mypinkpal.com");
            }
        }
    }

    /*public User getOccupantById(int occupantId) {
        User friend = UsersDatabaseManager.getUserById(context, occupantId);
        if (friend == null) {
            friend = new User();
            friend.setUserId(occupantId);
            friend.setFullName(occupantId + ConstsCore.EMPTY_STRING);
        }
        return friend;
    }*/

    public String getOccupantById(int occupantId) {
        User friend = UsersDatabaseManager.getUserById(context, occupantId);
        QBUser selfUser = AppSession.getSession().getUser();
        int selfId = selfUser.getId();
        if (friend == null) {
            if (selfId == occupantId)
                return selfUser.getFullName();
            friend = new User();
            friend.setUserId(occupantId);
            friend.setFullName(occupantId + ConstsCore.EMPTY_STRING);
        }
        return friend.getFullName();
    }

    public User getOccupantByIdAndFriend(int occupantId, Friend friend) {
        User user = UsersDatabaseManager.getUserById(context, occupantId);
        if (user == null) {
            user = new User();
            user.setUserId(occupantId);
            if (friend != null && occupantId == Integer.parseInt(friend.getQuickbloxid()) && friend.getFullname() != null)
                user.setFullName(friend.getFullname());
            else
                user.setFullName(occupantId + ConstsCore.EMPTY_STRING);
        } else if (user.getFullName() == null || user.getFullName().equals("***") == true) {
            if (friend != null && occupantId == Integer.parseInt(friend.getQuickbloxid()) && friend.getFullname() != null)
                user.setFullName(friend.getFullname());
            else
                user.setFullName(occupantId + ConstsCore.EMPTY_STRING);
        }

        user.setExternalId(friend.getUser_id());

        return user;
    }

    private static class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView nameTextView;
        public TextView createdTextview;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
    }
}
