package com.mgrin.thau.sessions;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.mgrin.thau.APIError;
import com.mgrin.thau.broadcaster.Broadcasted;
import com.mgrin.thau.broadcaster.BroadcastingService;
import com.mgrin.thau.broadcaster.BroadcastEvent.BroadcastEventType;
import com.mgrin.thau.configurations.ThauConfigurations;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.credentials.Credentials;
import com.mgrin.thau.credentials.CredentialService;
import com.mgrin.thau.sessions.authDto.FacebookAuthDTO;
import com.mgrin.thau.sessions.authDto.GitHubAuthDTO;
import com.mgrin.thau.sessions.authDto.GoogleAuthDTO;
import com.mgrin.thau.sessions.authDto.LinkedInAuthDTO;
import com.mgrin.thau.sessions.authDto.PasswordAuthDTO;
import com.mgrin.thau.sessions.authDto.TwitterAuthDTO;
import com.mgrin.thau.sessions.externalServices.FacebookService;
import com.mgrin.thau.sessions.externalServices.GitHubService;
import com.mgrin.thau.sessions.externalServices.GoogleService;
import com.mgrin.thau.sessions.externalServices.LinkedInService;
import com.mgrin.thau.sessions.externalServices.TwitterService;
import com.mgrin.thau.users.User;
import com.mgrin.thau.users.UserService;
import com.mgrin.thau.utils.TokenDTO;
import com.restfb.exception.FacebookOAuthException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
public class SessionAPI {

    public static final String JWT_HEADER = "x-thau-jwt";
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAPI.class);

    private SessionService sessions;

    private UserService userService;

    private CredentialService credentialService;

    private ThauConfigurations configurations;

    private FacebookService facebookService;

    private GoogleService googleService;

    private GitHubService githubService;

    private TwitterService twitterService;

    private LinkedInService linkedinService;

    private BroadcastingService broadcastingService;

    @Autowired
    public SessionAPI(SessionService sessions, UserService users, CredentialService credentials,
            ThauConfigurations configurations, FacebookService facebookService, GoogleService googleService,
            GitHubService githubService, TwitterService twitterService, LinkedInService linkedinService,
            BroadcastingService broadcastingService) {
        this.sessions = sessions;
        this.userService = users;
        this.credentialService = credentials;
        this.configurations = configurations;
        this.facebookService = facebookService;
        this.googleService = googleService;
        this.githubService = githubService;
        this.twitterService = twitterService;
        this.broadcastingService = broadcastingService;
        this.linkedinService = linkedinService;
    }

    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public Session getCurrentSession(@RequestHeader(name = SessionAPI.JWT_HEADER, required = false) String token) {
        if (token == null) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized: no token provided");
        }

        Optional<Session> opSession;
        try {
            opSession = sessions.getSessionFromToken(token);
        } catch (Exception e) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized: could not decrypt token", e);
        }

        if (!opSession.isPresent()) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized: no session found");
        }

        Session session = opSession.get();
        if (session.getStrategy() == Strategy.PASSWORD) {
            Optional<Credentials> opCredentials = credentialService.getByEmail(session.getUser().getEmail());
            if (!opCredentials.isPresent()) {
                throw new APIError(HttpStatus.FORBIDDEN, "User does not have password credentials");
            }
            Credentials credentials = opCredentials.get();
            if (configurations.getPasswordStrategyConfiguration().isRequireEmailVerification()
                    && !credentials.isVerified()) {
                throw new APIError(HttpStatus.FORBIDDEN, "Email is not yet validated");
            }
        }
        return session;
    }

    @PostMapping(path = "/password", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Broadcasted(type = BroadcastEventType.EXCHANGE_PASSWORD_FOR_TOKEN)
    public ResponseEntity<TokenDTO> createSession(@RequestBody PasswordAuthDTO auth) throws NoSuchAlgorithmException {
        Optional<Credentials> opCredentials = credentialService.getByEmailAndPassword(auth.getEmail(),
                auth.getPassword());
        if (!opCredentials.isPresent()) {
            throw new APIError(HttpStatus.NOT_FOUND, "User not found");
        }

        Credentials credentials = opCredentials.get();
        Session session = sessions.create(credentials.getUser(), Strategy.PASSWORD);
        String token = sessions.createJWTToken(session);
        ResponseEntity<TokenDTO> response = ResponseEntity.ok().header(JWT_HEADER, token).body(new TokenDTO(token));

        return response;
    }

    @PostMapping(path = "/facebook", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Broadcasted(type = BroadcastEventType.EXCHANGE_FACEBOOK_AUTH_TOKEN_FOR_TOKEN)
    public ResponseEntity<TokenDTO> createSession(@RequestBody FacebookAuthDTO auth) {
        if (auth.getAccessToken() == null) {
            throw new APIError(HttpStatus.NOT_FOUND, "User not found");
        }

        com.restfb.types.User facebookUser;
        try {
            facebookUser = facebookService.getFacebookUser(auth.getAccessToken());
        } catch (FacebookOAuthException e) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized", e);
        }

        String email = facebookUser.getEmail();

        Optional<User> opUser = userService.getByEmail(email);
        User user;
        if (!opUser.isPresent()) {
            user = User.of(facebookUser);
            user = userService.create(user, facebookUser);
        } else {
            user = opUser.get();
            userService.updateProvidersData(user, facebookUser);
        }

        Session session = sessions.create(user, Strategy.FACEBOOK);
        String token = sessions.createJWTToken(session);
        ResponseEntity<TokenDTO> response = ResponseEntity.ok().header(JWT_HEADER, token).body(new TokenDTO(token));

        return response;
    }

    @PostMapping(path = "/google", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Broadcasted(type = BroadcastEventType.EXCHANGE_GOOGLE_CODE_FOR_TOKEN)
    public ResponseEntity<TokenDTO> createSession(@RequestBody GoogleAuthDTO auth) {
        if (auth.getCode() == null) {
            throw new APIError(HttpStatus.NOT_FOUND, "User not found");
        }

        GoogleIdToken.Payload googleUser;
        try {
            googleUser = googleService.getGoogleUser(auth.getCode(), auth.getRedirectURI());
        } catch (IOException e) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized", e);
        }
        String email = (String) googleUser.get("email");

        Optional<User> opUser = userService.getByEmail(email);
        User user;
        if (!opUser.isPresent()) {
            user = User.of(googleUser);
            user = userService.create(user, googleUser);
        } else {
            user = opUser.get();
            userService.updateProvidersData(user, googleUser);
        }

        Session session = sessions.create(user, Strategy.GOOGLE);
        String token = sessions.createJWTToken(session);
        ResponseEntity<TokenDTO> response = ResponseEntity.ok().header(JWT_HEADER, token).body(new TokenDTO(token));

        return response;
    }

    @PostMapping(path = "/github", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Broadcasted(type = BroadcastEventType.EXCHANGE_GITHUB_CODE_FOR_TOKEN)
    public ResponseEntity<TokenDTO> createSession(@RequestBody GitHubAuthDTO auth) {
        if (auth.getCode() == null) {
            throw new APIError(HttpStatus.NOT_FOUND, "User not found");
        }

        GitHubService.GitHubUser githubUser;
        try {
            githubUser = githubService.getGitHubUser(auth.getCode());
        } catch (Exception e) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized", e);
        }

        String email = (String) githubUser.get("email");

        Optional<User> opUser = userService.getByEmail(email);
        User user;
        if (!opUser.isPresent()) {
            user = User.of(githubUser);
            user = userService.create(user, githubUser);
        } else {
            user = opUser.get();
            userService.updateProvidersData(user, githubUser);
        }

        Session session = sessions.create(user, Strategy.GITHUB);
        String token = sessions.createJWTToken(session);
        ResponseEntity<TokenDTO> response = ResponseEntity.ok().header(JWT_HEADER, token).body(new TokenDTO(token));

        return response;
    }

    @PostMapping(path = "/linkedin", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Broadcasted(type = BroadcastEventType.EXCHANGE_LINKEDIN_CODE_FOR_TOKEN)
    public ResponseEntity<TokenDTO> createSession(@RequestBody LinkedInAuthDTO auth) {
        if (auth.getCode() == null) {
            throw new APIError(HttpStatus.NOT_FOUND, "User not found");
        }

        LinkedInService.LinkedInUser linkedinUser;
        try {
            linkedinUser = linkedinService.getLinkedInUser(auth.getCode(), auth.getRedirectURI());
        } catch (Exception e) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized", e);
        }

        String email = (String) linkedinUser.get("email");

        Optional<User> opUser = userService.getByEmail(email);
        User user;
        if (!opUser.isPresent()) {
            user = User.of(linkedinUser);
            user = userService.create(user, linkedinUser);
        } else {
            user = opUser.get();
            userService.updateProvidersData(user, linkedinUser);
        }

        Session session = sessions.create(user, Strategy.GITHUB);
        String token = sessions.createJWTToken(session);
        ResponseEntity<TokenDTO> response = ResponseEntity.ok().header(JWT_HEADER, token).body(new TokenDTO(token));

        return response;
    }

    @PostMapping(path = "/twitter", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<TokenDTO> createSession(@RequestBody TwitterAuthDTO auth) {
        if (auth.getOauth_token() == null && auth.getRedirectURI() == null) {
            throw new APIError(HttpStatus.NOT_FOUND, "User not found");
        }

        String twitterRedirectURI;
        if (auth.getRedirectURI() != null) {
            try {
                twitterRedirectURI = twitterService.getTwitterRedirectURI(auth.getRedirectURI());
            } catch (Exception e) {
                throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized", e);
            }

            if (twitterRedirectURI != null) {
                throw new APIError(HttpStatus.FOUND, twitterRedirectURI);
            }
        }

        twitter4j.User twitterUser;
        try {
            twitterUser = twitterService.getTwitterUser(auth.getOauth_token(), auth.getOauth_verifier());
        } catch (Exception e) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized", e);
        }

        String email = twitterUser.getEmail();
        if (email == null) {
            LOGGER.warn(
                    "Your Twitter app was not Whitelisted and so the user's email is not available. For now, we'll use the fake email being <USERNAME>@twitter.thau");
            email = twitterUser.getScreenName() + "@twitter.thau";
        }
        Optional<User> opUser = userService.getByEmail(email);
        User user;
        if (!opUser.isPresent()) {
            user = User.of(twitterUser);
            user = userService.create(user, twitterUser);
        } else {
            user = opUser.get();
            userService.updateProvidersData(user, twitterUser);
        }

        Session session = sessions.create(user, Strategy.TWITTER);
        String token = sessions.createJWTToken(session);
        ResponseEntity<TokenDTO> response = ResponseEntity.ok().header(JWT_HEADER, token).body(new TokenDTO(token));
        broadcastingService.publish(BroadcastEventType.EXCHANGE_TWITTER_AUTH_TOKEN_FOR_TOKEN, response.getBody());
        return response;
    }
}