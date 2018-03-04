package com.manifestwebdesign.twitterconnect;

import io.fabric.sdk.android.Fabric;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetui.UserTimeline;

import retrofit.client.Response;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.mime.TypedByteArray;

public class TwitterConnect extends CordovaPlugin {

	private static final String LOG_TAG = "Twitter Connect";
	private String action;

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		Fabric.with(cordova.getActivity().getApplicationContext(), new Twitter(new TwitterAuthConfig(getTwitterKey(), getTwitterSecret())));
		Log.v(LOG_TAG, "Initialize TwitterConnect");
	}

	private String getTwitterKey() {
		return preferences.getString("TwitterConsumerKey", "");
	}

	private String getTwitterSecret() {
		return preferences.getString("TwitterConsumerSecret", "");
	}

	public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		Log.v(LOG_TAG, "Received: " + action);
		this.action = action;
		final Activity activity = this.cordova.getActivity();
		final Context context = activity.getApplicationContext();
		cordova.setActivityResultCallback(this);

		if (action.equals("login")) {
			login(activity, callbackContext);
			return true;
		}
		if (action.equals("logout")) {
			logout(callbackContext);
			return true;
		}
		if (action.equals("showUser")) {
			boolean includeEntities = false;
			String includeEntitiesStr = "";

			try {
				includeEntitiesStr = args.getJSONObject(0).getString("include_entities");
				includeEntities = Boolean.valueOf(includeEntitiesStr);
			} catch(JSONException e) {
				//empty since has default value if error occurs
			}
			showUser(includeEntities, callbackContext);
			return true;
		}
		if (action.equals("verifyCredentials")) {
			boolean includeEntities = false;
			boolean skipStatus = true;
			boolean includeEmail = true;
			String includeEntitiesStr = "";
			String skipStatusStr = "";
			String includeEmailStr = "";

			try {
				includeEntitiesStr = args.getJSONObject(0).getString("include_entities");
				includeEntities = Boolean.valueOf(includeEntitiesStr);
			} catch(JSONException e) {
				//empty since has default value if error occurs
			}

			try {
				skipStatusStr = args.getJSONObject(0).getString("skip_status");
				skipStatus = Boolean.valueOf(skipStatusStr);
			} catch(JSONException e) {
				//empty since has default value if error occurs
			}

			try {
				includeEmailStr = args.getJSONObject(0).getString("include_email");
				includeEmail = Boolean.valueOf(includeEmailStr);
			} catch(JSONException e) {
				//empty since has default value if error occurs
			}

			verifyCredentials(includeEntities, skipStatus, includeEmail, callbackContext);
			return true;
		}
		if (action.equals("sendTweet")) {
			String msg = args.getJSONObject(0).getString("status");
			sendTweet(msg, callbackContext);
			return true;
		}
		if (action.equals("openComposer")) {
			String defaultText = args.getString(0);
			if(defaultText == null) {
				callbackContext.error("A default text should be provided as an input parameter of the function call: openComposer!");
				return true;
			}
			openComposer(defaultText, activity, callbackContext);
			return true;
		}
		if (action.equals("showTimeline")) {
			String query = args.getString(0);
			if(query == null) {
				callbackContext.error("A query should be provided as an input parameter of the function call: showTimeline!");
				return true;
			}
			showTimeline(query, activity, callbackContext);
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

	private void showUser(final boolean includeEntities, final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				UserShowServiceApi twitterApiClient = new UserShowServiceApi(Twitter.getSessionManager().getActiveSession());
				UserShowService userShowService = twitterApiClient.getCustomService();
				userShowService.show(Twitter.getSessionManager().getActiveSession().getUserName(),
									includeEntities,
									new Callback<Response>() {
					@Override
					public void success(Result<Response> result) {
						try {
							callbackContext.success(new JSONObject(new String(((TypedByteArray) result.response.getBody()).getBytes())));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					@Override
					public void failure(TwitterException exception) {
						Log.v(LOG_TAG, "Twitter API Failed "+exception.getLocalizedMessage());
						callbackContext.error(exception.getLocalizedMessage());
					}
				});
			}
		});
	}

	private void verifyCredentials(final boolean includeEntities,
								   final boolean skipStatus,
								   final boolean includeEmail,
								   final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				VerifyCredentialsServiceApi twitterApiClient = new VerifyCredentialsServiceApi(Twitter.getSessionManager().getActiveSession());
				VerifyCredentialsService credentialsService = twitterApiClient.getCustomService();
				credentialsService.verify(includeEntities,
										skipStatus,
										includeEmail,
										new Callback<Response>() {
					@Override
					public void success(Result<Response> result) {
						try {
							callbackContext.success(new JSONObject(new String(((TypedByteArray) result.response.getBody()).getBytes())));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					@Override
					public void failure(TwitterException exception) {
						Log.v(LOG_TAG, "VerifyCredentials API call failed.");
						callbackContext.error(exception.getLocalizedMessage());
					}
				});
			}
		});
	}

	/**
	 * Extends TwitterApiClient adding our additional endpoints
	 * via the custom 'TweetService'
	 */
	class TweetServiceApi extends TwitterApiClient {
		public TweetServiceApi(TwitterSession session) {
			super(session);
		}

		public TweetService getCustomService() {
			return getService(TweetService.class);
		}
	}

	interface TweetService {
		@POST("/1.1/statuses/update.json")
		void tweet(@Query("status") String status, Callback<Response> cb);
	}

	private void sendTweet(final String msg, final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				TweetServiceApi twitterApiClient = new TweetServiceApi(Twitter.getSessionManager().getActiveSession());
				TweetService tweetService = twitterApiClient.getCustomService();
				tweetService.tweet(msg, new Callback<Response>() {
					@Override
					public void success(Result<Response> result) {
						try {
							callbackContext.success(new JSONObject(new String(((TypedByteArray) result.response.getBody()).getBytes())));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					@Override
					public void failure(TwitterException exception) {
						Log.v(LOG_TAG, "Twitter API Failed "+exception.getLocalizedMessage());
						callbackContext.error(exception.getLocalizedMessage());
					}
				});
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

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.v(LOG_TAG, "activity result: " + requestCode + ", code: " + resultCode);
		if (action.equals("login")) {
			handleLoginResult(requestCode, resultCode, intent);
		}
	}

	private void openComposer(final String text, final Activity activity, final CallbackContext callbackContext) {
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				TweetComposer.Builder builder = new TweetComposer.Builder(activity).text(text);
				builder.show();
				callbackContext.success();
			}
		});
	}

	private void showTimeline(final String query, final Activity activity, final CallbackContext callbackContext) {
		try {
			Context context = cordova.getActivity().getApplicationContext();
			Intent intent = new Intent(context, Class.forName("com.manifestwebdesign.twitterconnect.TimelineListActivity"));
			intent.putExtra("query", query);
			int resourceId = cordova.getActivity().getResources().getIdentifier("timeline", "layout", cordova.getActivity().getPackageName());
			intent.putExtra("resourceid", resourceId);
			cordova.startActivityForResult(this, intent, 1);
			callbackContext.success();
		}
		catch(Exception e) {
     		System.err.println("Exception: " + e.getMessage());
     		callbackContext.error(e.getMessage());
   		}
		
	}

}
