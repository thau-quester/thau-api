package com.mgrin.thau.sessions.externalServices;

import javax.annotation.PostConstruct;

import com.mgrin.thau.configurations.ThauConfigurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

@Component
public class TwitterService {
    @Autowired
    private ThauConfigurations configurations;

    private Twitter twitter;

    @PostConstruct
    private void createTwitterInstance() {
        twitter = TwitterFactory.getSingleton();
        try {
            twitter.setOAuthConsumer(configurations.getTwitterStrategyConfiguration().getClientId(),
                    configurations.getTwitterStrategyConfiguration().getClientSecret());
        } catch (IllegalStateException e) {
        }

    }

    public String getTwitterRedirectURI(String redirectURI) throws TwitterException {
        Twitter twitter = TwitterFactory.getSingleton();
        twitter.setOAuthAccessToken(null);
        RequestToken requestToken = twitter.getOAuthRequestToken(redirectURI);
        return requestToken.getAuthorizationURL();
    }

    public twitter4j.User getTwitterUser(String oauth_token, String oauth_verifier) throws TwitterException {
        Twitter twitter = TwitterFactory.getSingleton();
        AccessToken accecssToken = twitter.getOAuthAccessToken(oauth_verifier);
        twitter.setOAuthAccessToken(accecssToken);
        return twitter.showUser(accecssToken.getUserId());
    }
}
