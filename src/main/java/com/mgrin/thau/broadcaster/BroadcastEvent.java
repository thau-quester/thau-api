package com.mgrin.thau.broadcaster;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class BroadcastEvent {
    public enum BroadcastEventType {
        EXCHANGE_PASSWORD_FOR_TOKEN, EXCHANGE_GOOGLE_ID_TOKEN_FOR_TOKEN,
        EXCHANGE_FACEBOOK_AUTH_TOKEN_FOR_TOKEN, CREATE_NEW_USER_WITH_PASSWORD, USER_VALIDATED_EMAIL,
    }

    private BroadcastEventType type;
    private Object payload;

    public BroadcastEvent(BroadcastEventType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public static String toJson(BroadcastEvent event) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(event);
    }

    public BroadcastEventType getType() {
        return type;
    }

    public void setType(BroadcastEventType type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

}
