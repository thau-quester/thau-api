package com.mgrin.thau.sessions;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import com.mgrin.thau.APIError;
import com.mgrin.thau.broadcaster.Broadcasted;
import com.mgrin.thau.broadcaster.BroadcastEvent.BroadcastEventType;
import com.mgrin.thau.configurations.ThauConfigurations;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.credentials.Credentials;
import com.mgrin.thau.credentials.CredentialService;
import com.mgrin.thau.sessions.authDto.FacebookAuthDTO;
import com.mgrin.thau.sessions.authDto.PasswordAuthDTO;
import com.mgrin.thau.users.User;
import com.mgrin.thau.users.UserService;
import com.mgrin.thau.utils.TokenDTO;
import com.restfb.exception.FacebookOAuthException;

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

    private SessionService sessions;

    private UserService userService;

    private CredentialService credentialService;

    private ThauConfigurations configurations;

    private FacebookService facebookService;

    @Autowired
    public SessionAPI(SessionService sessions, UserService users, CredentialService credentials,
            ThauConfigurations configurations, FacebookService facebookService) {
        this.sessions = sessions;
        this.userService = users;
        this.credentialService = credentials;
        this.configurations = configurations;
        this.facebookService = facebookService;
    }

    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public Session getCurrentSessoin(@RequestHeader(name = SessionAPI.JWT_HEADER, required = false) String token) {
        if (token == null) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Optional<Session> opSession;
        try {
            opSession = sessions.getSessionFromToken(token);
        } catch (Exception e) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized", e);
        }

        if (!opSession.isPresent()) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized");
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
}