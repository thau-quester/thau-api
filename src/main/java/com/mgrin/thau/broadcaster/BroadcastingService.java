package com.mgrin.thau.broadcaster;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.mgrin.thau.configurations.ThauConfigurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BroadcastingService {

    @Autowired
    private ThauConfigurations configurations;

    private List<IBroadcastClient> clients = new LinkedList<>();
    
    @PostConstruct
    public void initializeBroadccastingClients() {
        if (configurations.getHttpBroadcastConfiguration() != null) {
            clients.add(new BroadcastHTTTPClient(configurations.getHttpBroadcastConfiguration()));
        }
    }

    public void publish(BroadcastEvent.BroadcastEventType event, Object payload) {
        clients.forEach(client -> client.publish(event, payload));
    }

}