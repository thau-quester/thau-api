package com.mgrin.thau.configurations.broadcast;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BroadcastChannel {
    HTTP("http"), KAFKA("kafka");

    private String value;

    BroadcastChannel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}