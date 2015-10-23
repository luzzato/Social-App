package org.codechimp.apprater;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.Html;
import android.util.Log;

public class AppRater {
	// Preference Constants
	private final static String PREF_NAME = "apprater";
	private final static String PREF_LAUNCH_COUNT = "launch_count";
	private final static String PREF_FIRST_LAUNCHED = "date_firstlaunch";
	private final static String PREF_DONT_SHOW_AGAIN = "dontshowagain";

	private final static int DAYS_UNTIL_PROMPT = 3;
	private final static int LAUNCHES_UNTIL_PROMPT = 3;
	private static String title = "", content = "", rate = "", later = "",
			no_thank = "";
	private static Market market = new GoogleMarket();

	/**
	 * Call this method at the end of your OnCreate method to determine whether
	 * to show the rate prompt using the default day and launch count values
	 * 
	 * @param context
	 */
	public void app_launched(Context context) {
		app_launched(context, DAYS_UNTIL_PROMPT, LAUNCHES_UNTIL_PROMPT);
	}

	public void setTextString(String title, String content, String rate,
			String later, String no_thanks) {
		this.title = Html.fromHtml(title).toString();
		this.content = Html.fromHtml(content).toString();
		this.rate = Html.fromHtml(rate).toString();
		this.later = Html.fromHtml(later).toString();
		this.no_thank = Html.fromHtml(no_thanks).toString();
	}

	/**
	 * Call this method at the end of your OnCreate method to determine whether
	 * to show the rate prompt
	 * 
	 * @param context
	 * @param daysUntilPrompt
	 * @param launchesUntilPrompt
	 */
	public void app_launched(Context context, int daysUntilPrompt,
			int launchesUntilPrompt) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, 0);
		if (prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
			return;
		}

		SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
		editor.putLong(PREF_LAUNCH_COUNT, launch_count);

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong(PREF_FIRST_LAUNCHED, 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
		}

		// Wait for at least the number of launches and the number of days used
		// until prompt
		if (launch_count >= launchesUntilPrompt) {
			if (System.currentTimeMillis() >= date_firstLaunch
					+ (daysUntilPrompt * 24 * 60 * 60 * 1000)) {
				showRateAlertDialog(context, editor);
			}
		}

		editor.commit();
	}

	/**
	 * Call this method directly if you want to force a rate prompt, useful for
	 * testing purposes
	 * 
	 * @param context
	 */
	public void showRateDialog(final Context context) {
		showRateAlertDialog(context, null);
	}

	/**
	 * Call this method directly to go straight to play store listing for rating
	 * 
	 * @param context
	 */
	public static void rateNow(final Context context) {
		context.startActivity(new Intent(Intent.ACTION_VIEW, market
				.getMarketURI(context)));
	}

	/**
	 * Set an alternate Market, defaults to Google Play
	 * 
	 * @param market
	 */
	public static void setMarket(Market market) {
		AppRater.market = market;
	}

	/**
	 * Get the currently set Market
	 * 
	 * @return market
	 */
	public static Market getMarket() {
		return market;
	}

	/**
	 * The meat of the library, actually shows the rate prompt dialog
	 */
	private static void showRateAlertDialog(final Context context,
			final SharedPreferences.Editor editor) {
		Builder builder = new AlertDialog.Builder(context);
		if (title.equals("")) {
			builder.setTitle(String.format(
					context.getString(R.string.dialog_title),
					context.getString(R.string.app_name)));
		} else {
			builder.setTitle(title);
		}

		if (content.equals("")) {
			builder.setMessage(String.format(
					context.getString(R.string.rate_message),
					context.getString(R.string.app_name)));
		} else {
			builder.setMessage(content);
		}
		String _rate = context.getString(R.string.rate);
		if (!rate.equals("")) {
			_rate = rate;
		}

		builder.setPositiveButton(_rate, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				context.startActivity(new Intent(Intent.ACTION_VIEW, market
						.getMarketURI(context)));
				if (editor != null) {
					editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
					editor.commit();
				}

				dialog.dismiss();
			}
		});
		String _later = context.getString(R.string.later);
		if (!later.equals("")) {
			_later = later;
		}
		builder.setNeutralButton(_later, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (editor != null) {
					Long date_firstLaunch = System.currentTimeMillis();
					editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		String _no_thanks = context.getString(R.string.no_thanks);
		if (!no_thank.equals("")) {
			_no_thanks = no_thank;
		}
		builder.setNegativeButton(_no_thanks,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (editor != null) {
							editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
							editor.commit();
						}
						dialog.dismiss();
					}
				});

		builder.show();
	}
}
