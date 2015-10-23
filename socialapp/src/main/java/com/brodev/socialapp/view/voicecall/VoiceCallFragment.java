package com.brodev.socialapp.view.voicecall;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.brodev.socialapp.utils.Consts;
import com.brodev.socialapp.view.imageview.RoundedImageView;
import com.brodev.socialapp.view.mediacall.OutgoingCallFragment;
import com.mypinkpal.app.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.User;

public class VoiceCallFragment extends OutgoingCallFragment {

    @Override
    protected void initUI(View rootView) {
        super.initUI(rootView);

            opponent=UsersDatabaseManager.getUserById(getActivity(),opponentcallingid);
            SharedPreferences profilepref = getActivity().getSharedPreferences("mypinkpal_friendlist", Context.MODE_PRIVATE);
            TextView timerTextView=((TextView) rootView.findViewById(R.id.timerTextView));
            timerTextView.setVisibility(View.INVISIBLE);
                    ((TextView) rootView.findViewById(R.id.name_textview)).setText(/*opponent*/ opponent.getFullName());
            TextView name_textview_process=((TextView) rootView.findViewById(R.id.name_textview_process));
            RoundedImageView avatarView = (RoundedImageView) rootView.findViewById(R.id.avatar_imageview);
            avatarView.setOval(true);
            if (!TextUtils.isEmpty(/*opponent*/ profilepref.getString(opponent.getEmail(), ""))) {
                Log.d("imageURL1",opponent.getAvatarUrl());
                ImageLoader.getInstance().displayImage(profilepref.getString(opponent.getEmail(), ""),
                        avatarView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
            }


    }

    @Override
    protected int getContentView() {
        return R.layout.activity_voice_call;
    }

}

