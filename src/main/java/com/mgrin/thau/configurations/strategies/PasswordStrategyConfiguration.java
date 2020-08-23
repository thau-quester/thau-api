package com.mgrin.thau.configurations.strategies;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression(
    "\"${thau.strategies.password.require_email_verification:0}\" != \"0\""
)
public class PasswordStrategyConfiguration {
    
    @Value("${thau.strategies.password.require_email_verification:true}")
    private boolean requireEmailVerification;
    
    public boolean isRequireEmailVerification() {
        return requireEmailVerification;
    }

}