package com.brodev.socialapp.fragment;

import android.os.Bundle;

import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.mypinkpal.app.R;

import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.android.pageradapter.VideoPagerAdapter;
import com.brodev.socialapp.entity.User;

public class VideoFragment extends SherlockFragment {
	public String type = null, itemId = "0", name = null;
	private PagerSlidingTabStrip tabs;
	private ColorView colorView;
	private User user;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		this.setHasOptionsMenu(true); 
		user = (User) getActivity().getApplicationContext();
		
		if (getArguments() != null) {
			type = getArguments().getString("type");
			itemId = getArguments().getString("itemId");
			name = getArguments().getString("name");
		}

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.view_pager, container, false);
		// Set the pager with an adapter
		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		pager.setAdapter(new VideoPagerAdapter(getFragmentManager(), type, itemId, name, getActivity().getApplicationContext()));

		// Bind the widget to the adapter
		tabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
		tabs.setViewPager(pager);
		
		colorView = new ColorView(tabs, getActivity().getApplicationContext());
		colorView.changeColorTabs(user.getColor());
		
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		//disable search menu item
		MenuItem search = menu.findItem(R.id.actionBar_chat);
		search.setVisible(false);
		
		inflater.inflate(R.menu.slide_bar, menu);
		 
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}
}