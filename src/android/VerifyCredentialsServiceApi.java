package com.manifestwebdesign.twitterconnect;

import com.twitter.sdk.android.core.*;

import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.client.Response;

public class VerifyCredentialsServiceApi extends TwitterApiClient {
    public VerifyCredentialsServiceApi(TwitterSession session) {
        super(session);
    }

    public VerifyCredentialsService getCustomService() {
        return getService(VerifyCredentialsService.class);
    }
}

interface VerifyCredentialsService {
    @GET("/1.1/account/verify_credentials.json")
    void verify(@Query("include_entities") boolean includeEntities,
                    @Query("skip_status") boolean skipStatus,
                    @Query("include_email") boolean includeEmail,
                    Callback<Response> cb);
}
