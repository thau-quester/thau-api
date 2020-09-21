package com.mgrin.thau.configurations.strategies;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Strategy {
    FACEBOOK("facebook"), GOOGLE("google"), PASSWORD("password"), GITHUB("github"), TWITTER("twitter");

    private String value;

    Strategy(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}