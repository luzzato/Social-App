package com.brodev.socialapp.view.chats;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brodev.socialapp.view.BaseActivity;
import com.brodev.socialapp.view.base.BaseListAdapter;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.mypinkpal.app.R;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class GroupDialogOccupantsAdapter extends BaseListAdapter<User> {
    List<User> userItems;
    public GroupDialogOccupantsAdapter(BaseActivity baseActivity, List<User> objectsList) {
        super(baseActivity, objectsList);
        userItems = objectsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        User user = getItem(position);
        Log.d("externaletestid",user.getExternalId());
        QBUser currendUser = AppSession.getSession().getUser();
        boolean status = false;
        /*ArrayList<User> userlist = new ArrayList<User>();
        userlist = ChatUtils.getJoinUsers();*/

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_dialog_friend, null);
            viewHolder = new ViewHolder();

            viewHolder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            viewHolder.profileTextView=(TextView)convertView.findViewById(R.id.profile_textview);
            viewHolder.name_textview_sex=(TextView)convertView.findViewById(R.id.name_textview_sex);
            viewHolder.onlineImageView = (ImageView) convertView.findViewById(R.id.online_imageview);
            //viewHolder.onlineImageView.setVisibility(View.GONE);
//            viewHolder.onlineStatusMessageTextView = (TextView) convertView.findViewById(
//                    R.id.statusMessageTextView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        String fullName;
        if (isFriend(user)) {
            fullName = user.getFullName();
//            viewHolder.onlineStatusMessageTextView.setVisibility(View.VISIBLE);
        } else {
            fullName = String.valueOf(user.getUserId());
//            viewHolder.onlineStatusMessageTextView.setVisibility(View.GONE);
        }

        viewHolder.nameTextView.setText(fullName);
        Log.d("compareid", String.valueOf(currendUser.getId()) + "/" + user.getExternalId());
        if(String.valueOf(currendUser.getExternalId()).equals(user.getExternalId())){
            SharedPreferences profilepref = baseActivity.getSharedPreferences("mypinkpal_user", Context.MODE_PRIVATE);
            //String age = profilepref.getString(user.getExternalId() + "_old", "");
            String location = profilepref.getString("userlocation", "");
            String age = profilepref.getString("age", "");
            String sexuality = profilepref.getString("sexuality", "");
            String userimageurl = profilepref.getString("userimageurl", "");
            viewHolder.profileTextView.setText(age+"," + location);
            user.setAvatarUrl(userimageurl);
           // Log.d("imageurlex",userimageurl);
            displayImage(user.getAvatarUrl(), viewHolder.avatarImageView);
            viewHolder.name_textview_sex.setText(sexuality);
        }else{
            SharedPreferences profilepref = baseActivity.getSharedPreferences("mypinkpal_friendlist", Context.MODE_PRIVATE);
            String age = profilepref.getString(user.getExternalId() + "_old", "");
            String location = profilepref.getString(user.getExternalId()+"_country", "");
            String sexuality = profilepref.getString(user.getExternalId()+"_sex", "");
            if((!age.equals(""))&&(!location.equals(""))) {
                viewHolder.profileTextView.setText(age + "," + location);
                viewHolder.name_textview_sex.setText(sexuality);
            }else{
                viewHolder.profileTextView.setText("Unknown");
            }
            if (user.getAvatarUrl() == null || user.getAvatarUrl().equals("") || user.getAvatarUrl().equals("null")) {
                SharedPreferences pref = baseActivity.getSharedPreferences("mypinkpal_friendlist", Context.MODE_PRIVATE);
                String aUrl = pref.getString(user.getExternalId(), "");
                user.setAvatarUrl(aUrl);
            }
            displayImage(user.getAvatarUrl(), viewHolder.avatarImageView);
        }
        setOnlineStatusVisibility(viewHolder, user);
        /*SharedPreferences totalnum = baseActivity.getSharedPreferences("online_number", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = totalnum.edit();
        editor.putInt("totalnum", total);
        editor.commit();*/
        // self account
        if (currendUser.getId() == user.getUserId()) {
            status = true;
        }
        for (int i = 0; i < userItems.size(); i++) {
            if (user.getUserId() == userItems.get(i).getUserId())
                status = true;
        }
        if (status == true)
            viewHolder.nameTextView.setTextColor(Color.BLACK);
        else
            viewHolder.nameTextView.setTextColor(Color.GRAY);



        /* get avatar url */
        //bronislaw
        /*if (user.getAvatarUrl() == null || user.getAvatarUrl().equals("") || user.getAvatarUrl().equals("null")) {
            SharedPreferences pref = baseActivity.getSharedPreferences("mypinkpal_friendlist", Context.MODE_PRIVATE);
            String aUrl = pref.getString(user.getExternalId(), "");
            user.setAvatarUrl(aUrl);
        }
        displayImage(user.getAvatarUrl(), viewHolder.avatarImageView);*/

        return convertView;
    }
    private void setOnlineStatusVisibility(ViewHolder viewHolder, User user) {
        if(isMe(user)) {
            user.setOnline(true);
        }

       //viewHolder.onlineStatusMessageTextView.setText(user.getOnlineStatus(baseActivity));
        if (user.isOnline()) {
           //viewHolder.onlineImageView.setVisibility(View.VISIBLE);
            viewHolder.onlineImageView.setImageResource(R.drawable.user_online);
      } else {
            viewHolder.onlineImageView.setImageResource(R.drawable.add_friend_online);
       }
    }
    private boolean isFriend(User user) {
        return user.getFullName() != null;
    }

    private boolean isMe(User inputUser) {
        QBUser currentUser = AppSession.getSession().getUser();
        return currentUser.getId() == inputUser.getUserId();
    }

    private static class ViewHolder {

        RoundedImageView avatarImageView;
        TextView nameTextView;
        ImageView onlineImageView;
        TextView profileTextView;
        TextView name_textview_sex;
//        TextView onlineStatusMessageTextView;
    }
}