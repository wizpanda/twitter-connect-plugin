package com.manifestwebdesign.twitterconnect;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.net.MalformedURLException;
import java.net.URL;

import io.fabric.sdk.android.Fabric;

public class TwitterConnect extends CordovaPlugin {

	private static final String LOG_TAG = "Twitter Connect";
	private String action;
	static final int COMPOSE_TWEET_RESULT = 1;
	private CallbackContext callback;

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		Fabric.with(cordova.getActivity().getApplicationContext(), new Twitter(new TwitterAuthConfig(getTwitterKey(), getTwitterSecret())), new TweetComposer());
		Log.v(LOG_TAG, "Initialize TwitterConnect");
	}

	private String getTwitterKey() {
		return preferences.getString("TwitterConsumerKey", "");
	}

	private String getTwitterSecret() {
		return preferences.getString("TwitterConsumerSecret", "");
	}

	public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		callback = callbackContext;
		Log.v(LOG_TAG, "Received: " + action);
		this.action = action;
		final Activity activity = this.cordova.getActivity();

		cordova.setActivityResultCallback(this);

		if (action.equals("login")) {
			login(activity, callbackContext);
			return true;
		}
		if (action.equals("logout")) {
			logout(callbackContext);
			return true;
		}
		if (action.equals("compose")) {
			composeTweet(activity, callbackContext, args);
			return true;
		}
		return false;
	}

	private void login(final Activity activity, final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				Twitter.logIn(activity, new Callback<TwitterSession>() {
					@Override
					public void success(Result<TwitterSession> twitterSessionResult) {
						Log.v(LOG_TAG, "Successful login session!");
						callbackContext.success(handleResult(twitterSessionResult.data));

					}

					@Override
					public void failure(TwitterException e) {
						Log.v(LOG_TAG, "Failed login session");
						callbackContext.error("Failed login session");
					}
				});
			}
		});
	}

	private void logout(final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				Twitter.logOut();
				Log.v(LOG_TAG, "Logged out");
				callbackContext.success();
			}
		});
	}

	private void composeTweet(final Activity activity, final CallbackContext callbackContext, final JSONArray args) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				TweetComposer.Builder builder = new TweetComposer.Builder(activity.getApplicationContext());
				if (args.length() > 0) {
					try {
						builder.text(args.getString(0));
					} catch (JSONException e) {
						//just squelch error
					}

					if (args.length() > 1) {
						try {
							builder.image(Uri.parse(args.getString(1)));
						} catch (JSONException e) {
							//just squelch error
						}
					}

					if (args.length() > 2) {
						try {
							builder.url(new URL(args.getString(2)));
						} catch (JSONException e) {
							//just squelch error
						} catch (MalformedURLException e) {
							//just squelch error
						}
					}
				}
				
				activity.startActivityForResult(builder.createIntent(), COMPOSE_TWEET_RESULT);
			}
		});
	}

	private JSONObject handleResult(TwitterSession result) {
		JSONObject response = new JSONObject();
		try {
			response.put("userName", result.getUserName());
			response.put("userId", result.getUserId());
			response.put("secret", result.getAuthToken().secret);
			response.put("token", result.getAuthToken().token);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response;
	}

	private void handleLoginResult(int requestCode, int resultCode, Intent intent) {
		TwitterLoginButton twitterLoginButton = new TwitterLoginButton(cordova.getActivity());
		twitterLoginButton.onActivityResult(requestCode, resultCode, intent);
	}

	private void handleComposeResult(int requestCode, int resultCode, Intent intent) {
		Log.v(LOG_TAG, "Compose finished: " + requestCode + " , " + resultCode);
		if (resultCode == Activity.RESULT_OK) {
			callback.success("Tweet sent");
		} else {
			callback.error("Cancelled compose");
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.v(LOG_TAG, "activity result: " + requestCode + ", code: " + resultCode);
		if (action.equals("login")) {
			handleLoginResult(requestCode, resultCode, intent);
		}
		if (action.equals("compose")) {
			handleComposeResult(requestCode, resultCode, intent);
		}
	}
}
