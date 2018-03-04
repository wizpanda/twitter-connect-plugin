package com.manifestwebdesign.twitterconnect;

import com.twitter.sdk.android.core.*;

import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.client.Response;

public class UserShowServiceApi extends TwitterApiClient {
	public UserShowServiceApi(TwitterSession session) {
		super(session);
	}

	public UserShowService getCustomService() {
		return getService(UserShowService.class);
	}
}

interface UserShowService {
	@GET("/1.1/users/show.json")
	void show(@Query("screen_name") String screen_name,
			@Query("include_entities") boolean includeEntities,
			Callback<Response> cb);
}
