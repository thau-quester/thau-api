package com.mgrin.thau.configurations.strategies;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import io.micrometer.core.lang.NonNull;

@Component
@ConditionalOnExpression(
    "\"${thau.strategies.google.client_id:0}\" != \"0\""
)
public class GoogleStrategyConfiguration {
    
    @Value("${thau.strategies.google.client_id}")
    @NonNull
    private String clientId;

    public String getClientId() {
        return clientId;
    }
}