package com.mgrin.thau.sessions.externalServices;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.mgrin.thau.configurations.ThauConfigurations;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LinkedInService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkedInService.class);

    private HttpClient client = HttpClient.newHttpClient();
    private static final String LINKEDIN_ACCESS_TOKEN_URI = "https://www.linkedin.com/oauth/v2/accessToken";
    private static final String LINKEDIN_USER_INFO_URI = "https://api.linkedin.com/v2/me?projection=(id,localizedFirstName,localizedLastName,profilePicture(displayImage~:playableStreams),vanityName)";
    private static final String LINKEDIN_USER_EMAIL_URI = "https://api.linkedin.com/v2/clientAwareMemberHandles?q=members&projection=(elements*(primary,type,handle~))";
    @Autowired
    private ThauConfigurations configurations;

    public LinkedInUser getLinkedInUser(String code, String redirectURI)
            throws URISyntaxException, IOException, InterruptedException {

        StringBuilder accessTokenURIBuilder = new StringBuilder();
        accessTokenURIBuilder.append("grant_type=authorization_code");
        accessTokenURIBuilder.append("&code=");
        accessTokenURIBuilder.append(code);
        accessTokenURIBuilder.append("&redirect_uri=");
        accessTokenURIBuilder.append(URLEncoder.encode(redirectURI, StandardCharsets.UTF_8.toString()));
        accessTokenURIBuilder.append("&client_id=");
        accessTokenURIBuilder.append(configurations.getLinkedinStrategyConfiguration().getClientId());
        accessTokenURIBuilder.append("&client_secret=");
        accessTokenURIBuilder.append(configurations.getLinkedinStrategyConfiguration().getClientSecret());

        HttpRequest.Builder accessTokenRequestBuilder = HttpRequest.newBuilder(new URI(LINKEDIN_ACCESS_TOKEN_URI))
                .header("Content-Type", "application/x-www-form-urlencoded").header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(accessTokenURIBuilder.toString()));

        HttpResponse<String> accessTokenResponse = this.client.send(accessTokenRequestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());
        JSONObject body = new JSONObject(accessTokenResponse.body());
        if (!body.has("access_token")) {
            throw new InterruptedException("No access token returned by linkedin");
        }

        String accessToken = (String) body.get("access_token");

        HttpRequest.Builder userInfoRequestBuilder = HttpRequest.newBuilder(new URI(LINKEDIN_USER_INFO_URI))
                .header("Accept", "application/json").header("Authorization", "Bearer " + accessToken).GET();
        HttpResponse<String> userInfoResponse = this.client.send(userInfoRequestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());

        JSONObject userInfo = new JSONObject(userInfoResponse.body());
        if (userInfo.has("serviceErrorCode")) {
            throw new InterruptedException((String) userInfo.get("message"));
        }

        try {
            HttpRequest.Builder userEmailRequestBuilder = HttpRequest.newBuilder(new URI(LINKEDIN_USER_EMAIL_URI))
                    .header("Accepr", "application/json").header("Authorization", "Bearer " + accessToken).GET();
            HttpResponse<String> userEmailResponse = this.client.send(userEmailRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());
            JSONObject userEmailResponseJSON = new JSONObject(userEmailResponse.body());
            JSONArray userEmailElements = (JSONArray) userEmailResponseJSON.get("elements");
            JSONObject userEmailElement = userEmailElements.getJSONObject(0);
            JSONObject userEmailHandle = (JSONObject) userEmailElement.get("handle~");
            String userEmail = userEmailHandle.getString("emailAddress");
            userInfo.put("emailAddress", userEmail);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn(
                    "User didn't share his email with you. For now, we'll use the fake email being <USER_ID>@linkedin.thau");
            userInfo.put("emailAddress", userInfo.get("id") + "@linkedin.thau");
        }

        LinkedInUser user = new LinkedInUser(userInfo);
        return user;
    }

    public static class LinkedInUser {
        private String original = "{\n";
        private String email;
        private String localizedFirstName;
        private String localizedLastName;
        private String profilePicture;
        private String vanityName;

        public LinkedInUser(JSONObject body) {
            for (String key : body.keySet()) {
                original += key + " : " + String.valueOf(body.get(key)) + ",\n";
            }
            original += "}";

            if (body.has("emailAddress")) {
                this.email = (String) body.get("emailAddress");
            }
            if (body.has("localizedFirstName")) {
                this.localizedFirstName = (String) body.get("localizedFirstName");
            }
            if (body.has("localizedLastName")) {
                this.localizedLastName = (String) body.get("localizedLastName");
            }
            if (body.has("vanityName")) {
                this.vanityName = (String) body.get("vanityName");
            }

            if (body.has("profilePicture")) {
                JSONObject piture = (JSONObject) body.get("profilePicture");
                JSONObject displayImage = (JSONObject) piture.get("displayImage~");
                JSONArray displayImageElements = (JSONArray) displayImage.get("elements");
                JSONObject displayImageElement = displayImageElements.getJSONObject(0);
                JSONArray identifiers = (JSONArray) displayImageElement.get("identifiers");
                JSONObject identifier = identifiers.getJSONObject(0);
                this.profilePicture = identifier.getString("identifier");
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

        public String getLocalizedFirstName() {
            return localizedFirstName;
        }

        public void setLocalizedFirstName(String localizedFirstName) {
            this.localizedFirstName = localizedFirstName;
        }

        public String getLocalizedLastName() {
            return localizedLastName;
        }

        public void setLocalizedLastName(String localizedLastName) {
            this.localizedLastName = localizedLastName;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }

        public String getVanityName() {
            return vanityName;
        }

        public void setVanityName(String vanityName) {
            this.vanityName = vanityName;
        }
    }
}
