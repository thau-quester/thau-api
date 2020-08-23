package com.mgrin.thau.broadcaster;

public interface IBroadcastClient {
    public void publish(BroadcastEvent.BroadcastEventType event, Object payload);
}