package com.mgrin.thau.sessions.externalServices;

import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;

import com.mgrin.thau.configurations.ThauConfigurations;
import com.restfb.DefaultFacebookClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FacebookService {

    @Autowired
    private ThauConfigurations configurations;

    public com.restfb.types.User getFacebookUser(String accessToken) {
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken,
                Version.getVersionFromString(configurations.getFacebookStrategyConfiguration().getGraphVersion()));
        com.restfb.types.User facebookUser = facebookClient.fetchObject("me", com.restfb.types.User.class,
                Parameter.with("fields", "id,first_name,last_name,email,birthday,gender,picture.type(large),link"));

        return facebookUser;
    }
}