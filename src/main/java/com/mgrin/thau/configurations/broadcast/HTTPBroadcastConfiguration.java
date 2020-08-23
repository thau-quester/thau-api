package com.mgrin.thau.configurations.broadcast;

import java.net.URL;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression(
    "\"${thau.broadcast.http.url:0}\" != \"0\""
)
public class HTTPBroadcastConfiguration {
    @Value("${thau.broadcast.http.url}")
    @NonNull
    private URL url;

    @Value("${thau.broadcast.http.headers:}")
    private String[] rawHeaders;

    private HttpHeaders headers;

    @PostConstruct
    public void assignHtttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        Arrays.stream(rawHeaders).forEach(header -> {
            String[] headerSplitted = header.split(":");
            headers.add(headerSplitted[0], headerSplitted[1]);
        });
        this.headers = headers;
    }


    public URL getUrl() {
        return url;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }
}