package com.mgrin.thau.broadcaster;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Broadcasted {
    public BroadcastEvent.BroadcastEventType type();
}