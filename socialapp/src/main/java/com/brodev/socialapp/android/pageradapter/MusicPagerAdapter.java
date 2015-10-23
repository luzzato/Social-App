package com.brodev.socialapp.android.pageradapter;


import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.fragment.MusicListFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MusicPagerAdapter extends FragmentStatePagerAdapter {

	private final int PAGES = 3;
	private String[] titles = new String[PAGES];
	private String _name, _type, _itemId;
	private PhraseManager phraseManager;
	
	public MusicPagerAdapter(FragmentManager fm, String type, String itemId, String name, Context context) {
		super(fm);
		phraseManager = new PhraseManager(context);
		_name = name;
		_type = type;
		_itemId = itemId;
		
		titles[0] = phraseManager.getPhrase(context, "music.all_songs");
		titles[1] = phraseManager.getPhrase(context, "music.my_songs");
		titles[2] = phraseManager.getPhrase(context, "music.friends_songs");
	}

	@Override
	public Fragment getItem(int position) {
		Fragment newContent = new MusicListFragment();
		Bundle bundle = new Bundle();
		bundle.putString("type", _type);
		bundle.putString("itemId", _itemId);
		bundle.putString("name", _name);
		
		switch (position) {
		case 0:
			bundle.putString("list", "all");
			break;
		case 1:
			bundle.putString("list", "my");
			break;
		case 2:
			bundle.putString("list", "friend");
			break;
		default:
			throw new IllegalArgumentException(
					"The item position should be less or equal to:" + PAGES);
		}
		newContent.setArguments(bundle);
		return newContent;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return titles[position];
	}

	@Override
	public int getCount() {
		return PAGES;
	}
}