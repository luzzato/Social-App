package com.brodev.socialapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mypinkpal.app.R;
import com.playerize.superrewards.SuperRewards;

/**
 * Created by Administrator on 8/21/2015.
 */
public class OfferFragement  extends Fragment {

    String userId = "sukhbir3433@gmail.com";
    String appHash = "lrjcnzoirmr.323286317223";
    Button btn_open_wall;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.offerwall, container, false);
        btn_open_wall=(Button)view.findViewById(R.id.btnoffer);

        btn_open_wall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                SuperRewards sr = new SuperRewards(getResources(),
                        "com.playerize.awesomeapp");
                sr.showOffers(getActivity(), appHash, userId);
            }
        });

        return view;
    }

}
