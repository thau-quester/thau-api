package com.mgrin.thau.configurations;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.mgrin.thau.configurations.broadcast.BroadcastChannel;
import com.mgrin.thau.configurations.broadcast.HTTPBroadcastConfiguration;
import com.mgrin.thau.configurations.jwt.JWTConfiguration;
import com.mgrin.thau.configurations.strategies.FacebookStrategyConfiguration;
import com.mgrin.thau.configurations.strategies.GoogleStrategyConfiguration;
import com.mgrin.thau.configurations.strategies.PasswordStrategyConfiguration;
import com.mgrin.thau.configurations.strategies.Strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ThauConfigurations {

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

    public HTTPBroadcastConfiguration getHttpBroadcastConfiguration() {
        return httpBroadcastConfiguration;
    }
}