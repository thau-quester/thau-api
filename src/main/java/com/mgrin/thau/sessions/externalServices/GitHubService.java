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
public class GitHubService {
    private HttpClient client = HttpClient.newHttpClient();

    private final String GITHUB_ACCESS_TOKEN_URI = "https://github.com/login/oauth/access_token";
    private final String GITHUB_USER_INFO_URI = "https://api.github.com/user";
    @Autowired
    private ThauConfigurations configurations;

    public GitHubUser getGitHubUser(String code) throws URISyntaxException, IOException, InterruptedException {
        Map<String, String> data = new HashMap<String, String>();
        data.put("client_id", configurations.getGitHubStrategyConfiguration().getClientId());
        data.put("client_secret", configurations.getGitHubStrategyConfiguration().getClientSecret());
        data.put("code", code);

        HashMapConverter converter = new HashMapConverter();

        HttpRequest.Builder accessTokenRequestBuilder = HttpRequest.newBuilder(new URI(GITHUB_ACCESS_TOKEN_URI))
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(converter.convertMapToJSONString(data)));

        HttpResponse<String> accessTokenResponse = this.client.send(accessTokenRequestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());
        Map<String, String> body = converter.convertJSONStringToMap(accessTokenResponse.body());
        String accessToken = body.get("access_token");

        HttpRequest.Builder userInfoRequestBuilder = HttpRequest.newBuilder(new URI(GITHUB_USER_INFO_URI))
                .header("Accept", "application/json").header("Authorization", "token " + accessToken).GET();
        HttpResponse<String> userInfoResponse = this.client.send(userInfoRequestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());
        GitHubUser userInfo = (GitHubUser) (converter.convertJSONStringToMap(userInfoResponse.body()));
        return userInfo;
    }

    public class GitHubUser extends HashMap<String, String> {
    }
}
