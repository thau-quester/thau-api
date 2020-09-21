package com.mgrin.thau.sessions.externalServices;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import com.mgrin.thau.configurations.ThauConfigurations;
import com.mgrin.thau.utils.HashMapConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LinkedInService {
    private HttpClient client = HttpClient.newHttpClient();
    private static final String LINKEDIN_ACCESS_TOKEN_URI = "https://www.linkedin.com/oauth/v2/accessToken";
    private static final String LINKEDIN_USER_INFO_URI = "https://api.linkedin.com/v2/me";

    @Autowired
    private ThauConfigurations configurations;

    public LinkedInUser getLinkedInUser(String code, String redirectURI)
            throws URISyntaxException, IOException, InterruptedException {
        HashMapConverter converter = new HashMapConverter();

        StringBuilder accessTokenURIBuilder = new StringBuilder();
        accessTokenURIBuilder.append("grant_type=authorization_code");
        accessTokenURIBuilder.append("&code=");
        accessTokenURIBuilder.append(code);
        accessTokenURIBuilder.append("&redirect_uri=");
        accessTokenURIBuilder.append(redirectURI);
        accessTokenURIBuilder.append("&client_id=");
        accessTokenURIBuilder.append(configurations.getLinkedinStrategyConfiguration().getClientId());
        accessTokenURIBuilder.append("&client_secret=");
        accessTokenURIBuilder.append(configurations.getLinkedinStrategyConfiguration().getClientSecret());

        HttpRequest.Builder accessTokenRequestBuilder = HttpRequest.newBuilder(new URI(LINKEDIN_ACCESS_TOKEN_URI))
                .header("Content-Type", "application/x-www-form-urlencoded").header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(accessTokenURIBuilder.toString()));

        HttpResponse<String> accessTokenResponse = this.client.send(accessTokenRequestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());
        System.out.println(accessTokenResponse.body());
        Map<String, String> body = converter.convertJSONStringToMap(accessTokenResponse.body());
        String accessToken = body.get("access_token");

        HttpRequest.Builder userInfoRequestBuilder = HttpRequest.newBuilder(new URI(LINKEDIN_USER_INFO_URI))
                .header("Accept", "application/json").header("Authorization", "token " + accessToken).GET();
        HttpResponse<String> userInfoResponse = this.client.send(userInfoRequestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());

        LinkedInUser user = (LinkedInUser) converter.convertJSONStringToMap(userInfoResponse.body());
        return user;
    }

    public class LinkedInUser extends HashMap<String, String> {
    }
}
