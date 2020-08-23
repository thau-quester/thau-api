package com.mgrin.thau.broadcaster;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

@Aspect
@Configuration
public class BroadcastingAspect {
        
    @Autowired
    private BroadcastingService broadcaster;

    @AfterReturning(value = "@annotation(com.mgrin.thau.broadcaster.Broadcasted)", returning = "response")
    public void broadcast(JoinPoint joinPoint, Object response) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
    
            Broadcasted annotation = method.getAnnotation(Broadcasted.class);
    
            Object payload = response;
            if (payload instanceof ResponseEntity) {
                payload = ((ResponseEntity<?>)response).getBody();
            }
    
            broadcaster.publish(annotation.type(), payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}