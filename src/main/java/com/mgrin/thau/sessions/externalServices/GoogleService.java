package com.mgrin.thau.sessions.externalServices;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.mgrin.thau.configurations.ThauConfigurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleService {
    @Autowired
    private ThauConfigurations configurations;

    public GoogleIdToken.Payload getGoogleUser(String authCode, String redirectUri) throws IOException {
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(), "https://oauth2.googleapis.com/token",
                configurations.getGoogleStrategyConfiguration().getClientId(),
                configurations.getGoogleStrategyConfiguration().getClientSecret(), authCode, redirectUri).execute();

        GoogleIdToken.Payload payload = tokenResponse.parseIdToken().getPayload();

        return payload;
    }
}