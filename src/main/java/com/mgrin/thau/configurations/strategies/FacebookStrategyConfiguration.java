package com.mgrin.thau.configurations.strategies;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import io.micrometer.core.lang.NonNull;

@Component
@ConditionalOnExpression(
    "\"${thau.strategies.facebook.client_id:0}\" != \"0\" and \"${thau.strategies.facebook.client_secret:0}\" != \"0\""
)
public class FacebookStrategyConfiguration {
    @Value("${thau.strategies.facebook.client_id}")
    @NonNull
    private String clientId;

    @Value("${thau.strategies.facebook.client_secret}")
    @NonNull
    private String clientSecret;

    @Value("${thau.strategies.facebook.graph_version:v7.0}")
    private String graphVersion;


    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getGraphVersion() {
        return graphVersion;
    }

}