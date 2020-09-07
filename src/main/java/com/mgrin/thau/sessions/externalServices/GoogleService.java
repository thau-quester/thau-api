package com.mgrin.thau.sessions.externalServices;

import com.mgrin.thau.configurations.ThauConfigurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleService {
    @Autowired
    private ThauConfigurations configurations;

    public void getGoogleUser(String idToken) {

    }
}