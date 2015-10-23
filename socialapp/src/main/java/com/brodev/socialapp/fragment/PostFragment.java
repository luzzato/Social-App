package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import org.json.JSONObject;


import com.actionbarsherlock.app.SherlockListFragment;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.entity.UserComposeSearch;
import com.brodev.socialapp.http.NetworkUntil;

import com.brodev.socialapp.android.PhraseManager;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PostFragment extends SherlockListFragment {

	private User user;
	private NetworkUntil network = new NetworkUntil();
	private EditText searchTxt, writeMessageTxt;
	private String URL_SEND_MAIL = null;
	private int sUserId;
	private String parent_feed_id;
	private String parent_module_id;
	private LinearLayout send_friends;
	private PhraseManager phraseManager;
	String content = null;
	UserSearchAdapter ca;
	int t = 0;
	String texta;
	int current;
	private int i = 0;
	private String[] Friends;
	private String post_type;
	ArrayList<String> ToFriends;
	private Timer tTimer = new Timer();
	private ImageView shareImg;
	private ColorView colorView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		colorView = new ColorView(getActivity().getApplicationContext());
		user = (User) getActivity().getApplicationContext();
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {		
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//create view from friend_fragment xml
		final View view = inflater.inflate(R.layout.share_post, container, false);
		
		send_friends = (LinearLayout)view.findViewById(R.id.send_friends);
		searchTxt = (EditText)view.findViewById(R.id.searchEdit);
		writeMessageTxt = (EditText)view.findViewById(R.id.share_post_text);
		writeMessageTxt.setHint(phraseManager.getPhrase(getActivity().getApplicationContext(), "share.message"));
		
		shareImg = (ImageView) view.findViewById(R.id.post_comment);
		colorView.changeColorShare(shareImg, user.getColor());
		
		final LinearLayout linearlayout2 = (LinearLayout)view.findViewById(R.id.share_post_layout);
		
		setHasOptionsMenu(true);
		ToFriends = new ArrayList<String>();
		getActivity().setProgressBarIndeterminateVisibility(false);
		//get user info
		user = (User) getActivity().getApplication().getApplicationContext();
		
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			parent_feed_id = extras.getString("parent_feed_id");
			parent_module_id = extras.getString("parent_module_id");
		}
		
		// change title
		
		//action change text in edit text
		searchTxt.addTextChangedListener(new TextWatcher() { 
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}
			@Override
			public void afterTextChanged(final Editable s) {
				if (s.length() != 0) {
					tTimer.cancel();
					tTimer = new Timer();
					tTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							if(s.toString().trim().length() > 0) {
								new UserListSearchTask().execute(s.toString().trim());
							}
						}
					}, 500);
				}
				
			}
		});
		
		RadioButton radioFriendButton = (RadioButton)view.findViewById(R.id.radioFriend);
		radioFriendButton.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "share.on_a_friend_s_wall"));
		RadioButton radioYourButton = (RadioButton)view.findViewById(R.id.radioYour);
		radioYourButton.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "share.on_your_wall"));
		
		if(radioFriendButton.isChecked()){
			linearlayout2.setVisibility(View.VISIBLE);
		} else {
			linearlayout2.setVisibility(View.GONE);
		}
		
		radioFriendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				post_type = "2";
				linearlayout2.setVisibility(View.VISIBLE);
			}
		});
		
		radioYourButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				post_type = "1";
				linearlayout2.setVisibility(View.GONE);
			}
		});
		
		return view;
	}


    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.poststatus, menu);
	 
		super.onCreateOptionsMenu(menu, inflater);
    }

	
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		 switch (item.getItemId()) {
		 	case R.id.action_post:
		 		getActivity().setProgressBarIndeterminateVisibility(true);
				content = writeMessageTxt.getText().toString();
				if (content.trim().length() > 0) {
					new composeTask().execute(String.valueOf(sUserId), content);	
				}
				writeMessageTxt.setText("");
				getActivity().finish();
			break; 
		 }
		return false;
	}
	
	@Override
	public void onListItemClick(ListView listview, View view, int position, long id) {
		UserComposeSearch user = (UserComposeSearch) getListAdapter().getItem(position);
		if (!ToFriends.contains(String.valueOf(user.getUserId()))) {
			//set full name 
			
			LayoutInflater layoutInflater = getActivity().getLayoutInflater();
		    View view_friend;
		    
	    
//	            // Add the text layout to the parent layout
		    view_friend = layoutInflater.inflate(R.layout.chips_adapter, send_friends, false);
		    

	            // In order to get the view we have to use the new view with text_layout in it
		    	RelativeLayout friend_tag = (RelativeLayout)view_friend.findViewById(R.id.post_friend_view);
		    	ImageView user_image = (ImageView)view_friend.findViewById(R.id.tag_imageView);
		    	TextView full_name = (TextView)view_friend.findViewById(R.id.tag_textView);
		    	ImageView delete = (ImageView)view_friend.findViewById(R.id.tag_delete);
		    	network.drawImageUrl(user_image, user.getImage(), R.drawable.loading);
		    	
		    	full_name.setText(user.getFullname().toString());
		    	
		    	ToFriends.add(String.valueOf(user.getUserId()));
		    	
		    	send_friends.addView(friend_tag);
		    	
		    	delete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						System.out.println(i);
						send_friends.removeViewAt(i-1);
						ToFriends.remove(i-1);
						i= i-1;
						
					}
				});
		    	if(i== 0){
		    		i=1;
		    	}else{
		    		i++;
		    	}
		}
		
	    	
		//focus to edit text write message
		//writeMessageTxt.requestFocus();
	}
	
	/**
	 * Send email task
	 * 
	 * @author ducpham
	 */
	public class composeTask extends AsyncTask<String, Void, String> {
		
		private ProgressDialog mProgressDialog;
		String result = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setMessage("Post...");
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			// String url
			if (isCancelled()) {
				return null;
			}
			
			try {
				
				if (Config.CORE_URL == null) {
					URL_SEND_MAIL = Config.makeUrl(user.getCoreUrl(), "postShare", true) + "&token=" + user.getTokenkey();
				} else {
					URL_SEND_MAIL = Config.makeUrl(Config.CORE_URL, "postShare", true) + "&token=" + user.getTokenkey();
				}
			
				// Use BasicNameValuePair to store POST data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				
				pairs.add(new BasicNameValuePair("post_type", post_type));
				
				if("2".equals(post_type)){
					Friends =  ToFriends.toArray(new String[ToFriends.size()]);
					JSONArray FriendObje = new JSONArray();
					for (String string : Friends) {
						FriendObje.put(string);
					}
					pairs.add(new BasicNameValuePair("friends", FriendObje.toString()));
				}
				pairs.add(new BasicNameValuePair("post_content", params[1]));
				pairs.add(new BasicNameValuePair("parent_feed_id", parent_feed_id));
				pairs.add(new BasicNameValuePair("parent_module_id", parent_module_id));
	
				result = network.makeHttpRequest(URL_SEND_MAIL, "POST", pairs);
						
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) 
			{
				try {
					getActivity().finish();
					
				} catch(Exception ex) {
					
				}
						
			}
		}
	}
	
	/**
	 * Class execute get search list user
	 * 
	 * @author ducpham
	 */
	public class UserListSearchTask extends AsyncTask<String, Void, String> {
		String result = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			if (isCancelled()) {
				return null;
			}
			// Simulates a background task
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			// get result from get method
			result = getResultFromGET(params[0]);

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				ca = new UserSearchAdapter(getActivity());
				
				ca = getUserAdapter(ca, result);

				if (ca != null) {
					setListAdapter(ca);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * function get result from get method
	 * @param sSearch
	 * @return result
	 */
	public String getResultFromGET(String sSearch) {

		String resultstring;

		// Use BasicNameValuePair to create GET data
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
		pairs.add(new BasicNameValuePair("method", "accountapi.searchFriend"));
		pairs.add(new BasicNameValuePair("search_for", sSearch));
		
		// url request
		String URL = null;
		if (Config.CORE_URL == null) {
			URL = Config.makeUrl(user.getCoreUrl(), null, false);	
		} else {
			URL = Config.makeUrl(Config.CORE_URL, null, false);
		}
		
		// request GET method to server
		resultstring = network.makeHttpRequest(URL, "GET", pairs);

		return resultstring;
	}
	
	/**
	 * Get Adapter from json
	 * @param ucs
	 * @param resString
	 * @return adapter
	 */
	public UserSearchAdapter getUserAdapter(UserSearchAdapter ucs, String resString) 
	{
		if (resString != null) {
			try {
				JSONObject mainJSON = new JSONObject(resString);
				// get output json array
				Object intervention = mainJSON.get("output");
				
				JSONObject objJSON = null;
				
				if (intervention instanceof JSONObject) 
				{
					JSONObject outputJSON = (JSONObject) intervention;
					
					for (int i = 0; i < outputJSON.length(); i++) {
						objJSON = outputJSON.getJSONObject(outputJSON.names().getString(i));
						UserComposeSearch uc = new UserComposeSearch();
						uc = getUser(uc, objJSON);
						// add user list to adapter
						ucs.add(uc);
					}
				} else if (intervention instanceof JSONArray) {
					JSONArray outputJSON = (JSONArray) intervention;
					
					for (int i = 0; i < outputJSON.length(); i++) {
						objJSON = outputJSON.getJSONObject(i);
						UserComposeSearch uc = new UserComposeSearch();
						uc = getUser(uc, objJSON);
						//add user list to adapter
						ucs.add(uc);
					}
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return ucs;
	}
	
	/**
	 * 
	 * @param output
	 * @return uc
	 */
	public UserComposeSearch getUser(UserComposeSearch uc, JSONObject output) 
	{
		try {
			// set user image
			uc.setImage(output.getString("user_image"));
			// set full name
			uc.setFullname(output.getString("full_name"));
			//set user id
			uc.setUserId(Integer.parseInt(output.getString("user_id")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return uc;
	}
	
	/**
	 * Create List user search adapter
	 * 
	 * @author ducpham
	 */
	public class UserSearchAdapter extends ArrayAdapter<UserComposeSearch> {
		
		public UserSearchAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			UserComposeSearch item = getItem(position);
			UserSearchViewHolder holder = null;
			
			if (view == null) {
				int layout = R.layout.share_list_user_search;

				view = LayoutInflater.from(getContext()).inflate(layout, null);
				// call element from xml
				ImageView icon = (ImageView) view.findViewById(R.id.image_friend);
				TextView content = (TextView) view.findViewById(R.id.title);
				view.setTag(new UserSearchViewHolder(icon, content));
			}
			
			if (holder == null && view != null) {
				Object tag = view.getTag();
				if (tag instanceof UserSearchViewHolder) {
					holder = (UserSearchViewHolder) tag;
				}
			}
			
			if (item != null && holder != null) {
				// set image friend;
				if (holder.imageHolder != null) {
					if (!"".equals(item.getImage())) {
						network.drawImageUrl(holder.imageHolder, item.getImage(), R.drawable.loading);
					}
				}
				// set full name
				if (holder.text != null) {
					holder.text.setText(item.getFullname());
				}
			}
			
			return view;
		}
	}
	
	/**
	 * Class list user - search view holder
	 * 
	 * @author ducpham
	 */
	public class UserSearchViewHolder {
		public final ImageView imageHolder;
		public final TextView text;

		public UserSearchViewHolder(ImageView icon, TextView text) {
			this.imageHolder = icon;
			this.text = text;
		}
	}
	
}
