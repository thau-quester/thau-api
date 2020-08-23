package com.mgrin.thau.configurations;

import java.util.List;

import com.mgrin.thau.configurations.broadcast.BroadcastChannel;
import com.mgrin.thau.configurations.strategies.Strategy;

public class PublicConfigurationDTO {
    private final String environment;
    private final String appName;
    private final List<BroadcastChannel> broadcastChannels;
    private final List<Strategy> availableStrategies;
    private final String encryptionAlgorithm;

    public PublicConfigurationDTO(ThauConfigurations configurations) {
        this.environment = configurations.getEnvironment();
        this.broadcastChannels = configurations.getBroadcastChannels();
        this.availableStrategies = configurations.getAvailableStrategies();
        this.encryptionAlgorithm = configurations.getJWTConfiguration().getEncryptionAlgorithm();
        this.appName = configurations.getAppName();
    }

    public String getEnvironment() {
        return environment;
    }

    public String getAppName() {
        return appName;
    }

    public List<BroadcastChannel> getBroadcastChannels() {
        return broadcastChannels;
    }

    public List<Strategy> getAvailableStrategies() {
        return availableStrategies;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }
}
