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

import org.json.JSONObject;
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

        HttpRequest.Builder accessTokenRequestBuilder = HttpRequest.newBuilder(new URI(GITHUB_ACCESS_TOKEN_URI))
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new JSONObject(data).toString()));

        HttpResponse<String> accessTokenResponse = this.client.send(accessTokenRequestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());
        JSONObject body = new JSONObject(accessTokenResponse.body());
        String accessToken = (String) body.get("access_token");

        HttpRequest.Builder userInfoRequestBuilder = HttpRequest.newBuilder(new URI(GITHUB_USER_INFO_URI))
                .header("Accept", "application/json").header("Authorization", "token " + accessToken).GET();
        HttpResponse<String> userInfoResponse = this.client.send(userInfoRequestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());
        GitHubUser userInfo = new GitHubUser(new JSONObject(userInfoResponse.body()));
        return userInfo;
    }

    public static class GitHubUser {
        private String original = "{\n";
        private String email;
        private String name;
        private String avatarUrl;
        private String htmlUrl;

        public GitHubUser(JSONObject body) {
            for (String key : body.keySet()) {
                original += key + " : " + String.valueOf(body.get(key)) + ",\n";
            }
            original += "}";
            if (body.has("email")) {
                this.email = (String) body.get("email");
            }
            if (body.has("name")) {
                this.name = (String) body.get("name");
            }
            if (body.has("avatar_url")) {
                this.avatarUrl = (String) body.get("avatar_url");
            }
            if (body.has("html_url")) {
                this.htmlUrl = (String) body.get("html_url");
            }
        }

        public String getOriginal() {
            return original;
        }

        public void setOriginal(String original) {
            this.original = original;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }
    }
}
