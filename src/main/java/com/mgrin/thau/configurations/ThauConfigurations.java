package com.mgrin.thau.configurations;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mgrin.thau.configurations.broadcast.BroadcastChannel;
import com.mgrin.thau.configurations.broadcast.HTTPBroadcastConfiguration;
import com.mgrin.thau.configurations.jwt.JWTConfiguration;
import com.mgrin.thau.configurations.strategies.FacebookStrategyConfiguration;
import com.mgrin.thau.configurations.strategies.GitHubStrategyConfiguration;
import com.mgrin.thau.configurations.strategies.GoogleStrategyConfiguration;
import com.mgrin.thau.configurations.strategies.PasswordStrategyConfiguration;
import com.mgrin.thau.configurations.strategies.Strategy;
import com.mgrin.thau.configurations.strategies.TwitterStrategyConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ThauConfigurations {

    @Value("${app.version}")
    private String apiVersion;

    @Value("${thau.env}")
    private String environment;

    @Value("${thau.app.name}")
    private String appName;

    @Value("${thau.app.cors}")
    private boolean corsEnabled;

    @Autowired
    private JWTConfiguration jwtConfiguration;

    @Autowired(required = false)
    private PasswordStrategyConfiguration passwordStrategyConfiguration;

    @Autowired(required = false)
    private GoogleStrategyConfiguration googleStrategyConfiguration;

    @Autowired(required = false)
    private FacebookStrategyConfiguration facebookStrategyConfiguration;

    @Autowired(required = false)
    private GitHubStrategyConfiguration githubStrategyConfiguration;

    @Autowired(required = false)
    private TwitterStrategyConfiguration twitterStrategyConfiguration;

    @Autowired(required = false)
    @JsonIgnore
    private HTTPBroadcastConfiguration httpBroadcastConfiguration;

    private List<Strategy> availableStrategies;
    private List<BroadcastChannel> broadcastChannels;

    @PostConstruct
    public void assignAvailableStrategies() {
        List<Strategy> availableStrategies = new LinkedList<>();
        if (googleStrategyConfiguration != null) {
            availableStrategies.add(Strategy.GOOGLE);
        }

        if (facebookStrategyConfiguration != null) {
            availableStrategies.add(Strategy.FACEBOOK);
        }

        if (passwordStrategyConfiguration != null) {
            availableStrategies.add(Strategy.PASSWORD);
        }

        if (githubStrategyConfiguration != null) {
            availableStrategies.add(Strategy.GITHUB);
        }

        if (twitterStrategyConfiguration != null) {
            availableStrategies.add(Strategy.TWITTER);
        }

        this.availableStrategies = availableStrategies;
    }

    @PostConstruct
    public void assignBroadcastChannels() {
        List<BroadcastChannel> broadcastChannels = new LinkedList<>();
        if (httpBroadcastConfiguration != null) {
            broadcastChannels.add(BroadcastChannel.HTTP);
        }

        this.broadcastChannels = broadcastChannels;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getAppName() {
        return appName;
    }

    public boolean isCORSEnabled() {
        return corsEnabled;
    }

    public List<BroadcastChannel> getBroadcastChannels() {
        return broadcastChannels;
    }

    public List<Strategy> getAvailableStrategies() {
        return availableStrategies;
    }

    public JWTConfiguration getJWTConfiguration() {
        return jwtConfiguration;
    }

    public PasswordStrategyConfiguration getPasswordStrategyConfiguration() {
        return passwordStrategyConfiguration;
    }

    public GoogleStrategyConfiguration getGoogleStrategyConfiguration() {
        return googleStrategyConfiguration;
    }

    public FacebookStrategyConfiguration getFacebookStrategyConfiguration() {
        return facebookStrategyConfiguration;
    }

    public GitHubStrategyConfiguration getGitHubStrategyConfiguration() {
        return githubStrategyConfiguration;
    }

    public HTTPBroadcastConfiguration getHttpBroadcastConfiguration() {
        return httpBroadcastConfiguration;
    }
}