package com.mgrin.thau.broadcaster;

import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mgrin.thau.configurations.broadcast.HTTPBroadcastConfiguration;

public class BroadcastHTTTPClient implements IBroadcastClient {

    private HTTPBroadcastConfiguration configurations;
    private HttpClient client;

    public BroadcastHTTTPClient(HTTPBroadcastConfiguration configurations) {
        client = HttpClient.newHttpClient();
        this.configurations = configurations;
    }

    @Override
    public void publish(BroadcastEvent.BroadcastEventType type, Object payload) {
        try {
            BroadcastEvent event = new BroadcastEvent(type, payload);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(configurations.getUrl().toURI())
                .POST(HttpRequest.BodyPublishers.ofString(BroadcastEvent.toJson(event)));

            Map<String, String> headersAsMap = this.configurations.getHeaders().toSingleValueMap();
            for (String header : headersAsMap.keySet()) {
                requestBuilder.header(header, headersAsMap.get(header));
            }

            client.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.discarding());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}