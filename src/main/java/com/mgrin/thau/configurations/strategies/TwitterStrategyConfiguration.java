package com.mgrin.thau.configurations.strategies;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import io.micrometer.core.lang.NonNull;

@Component
@ConditionalOnExpression("\"${thau.strategies.twitter.client_id:0}\" != \"0\" and \"${thau.strategies.twitter.client_secret:0}\" != \"0\"")
public class TwitterStrategyConfiguration {
    @Value("${thau.strategies.twitter.client_id}")
    @NonNull
    private String clientId;

    @Value("${thau.strategies.twitter.client_secret}")
    @NonNull
    @JsonIgnore
    private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}