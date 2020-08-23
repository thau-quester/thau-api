package com.mgrin.thau.users;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mgrin.thau.APIError;
import com.mgrin.thau.broadcaster.Broadcasted;
import com.mgrin.thau.broadcaster.BroadcastEvent.BroadcastEventType;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.credentials.Credentials;
import com.mgrin.thau.credentials.CredentialService;
import com.mgrin.thau.sessions.Session;
import com.mgrin.thau.sessions.SessionAPI;
import com.mgrin.thau.sessions.SessionService;
import com.mgrin.thau.utils.TokenDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserAPI {

    private UserService users;

    private CredentialService credentials;

    private SessionService sessions;

    @Autowired
    public UserAPI(UserService users, CredentialService credentials, SessionService sessions) {
        this.users = users;
        this.credentials = credentials;
        this.sessions = sessions;
    }

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    @Broadcasted(type = BroadcastEventType.CREATE_NEW_USER_WITH_PASSWORD)
    public ResponseEntity<TokenDTO> createUser(@RequestBody UserInputDTO userInfo) {
        if (userInfo.getUser() == null) {
            throw new APIError(HttpStatus.BAD_REQUEST, "Bad request");
        }

        Optional<User> opCreatedUser = users.getByEmail(userInfo.getUser().getEmail());
        User createdUser;

        if (!opCreatedUser.isPresent()) {
            createdUser = users.create(userInfo.getUser(), userInfo.getPassword());
        } else {
            throw new APIError(HttpStatus.BAD_REQUEST, "User already registered");
        }

        Session session = sessions.create(createdUser, Strategy.PASSWORD);
        String token = sessions.createJWTToken(session);
        ResponseEntity<TokenDTO> response = ResponseEntity.ok().header(SessionAPI.JWT_HEADER, token)
                .body(new TokenDTO(token));
        return response;
    }

    @PutMapping(path = "/{userId}", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<User> updateUser(@RequestBody User user, @PathVariable(value = "userId") long userId,
            @RequestHeader(name = SessionAPI.JWT_HEADER, required = false) String token)
            throws JsonMappingException, JsonProcessingException {
        Optional<Session> opSession = sessions.getSessionFromToken(token);
        if (!opSession.isPresent()) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        Session session = opSession.get();
        if (session.getUser().getId() != userId) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        User existingUser = session.getUser();
        user.setId(existingUser.getId());
        user.setEmail(existingUser.getEmail());
        user.setCreatedAt(existingUser.getCreatedAt());
        users.update(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping(path = "/{userId}", produces = { MediaType.APPLICATION_JSON_VALUE })
    public User getById(@PathVariable(value = "userId") long userId,
            @RequestHeader(name = SessionAPI.JWT_HEADER, required = false) String token)
            throws JsonMappingException, JsonProcessingException {
        Optional<Session> opSession = sessions.getSessionFromToken(token);
        if (!opSession.isPresent()) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        Optional<User> opUser = users.getById(userId);
        if (!opUser.isPresent()) {
            throw new APIError(HttpStatus.NOT_FOUND, "User not found");
        }
        return opUser.get();
    }

    @GetMapping(path = "/verification", produces = { MediaType.APPLICATION_JSON_VALUE })
    @Broadcasted(type = BroadcastEventType.USER_VALIDATED_EMAIL)
    public ResponseEntity<Boolean> validateUserEmail(@RequestParam String code) {
        Optional<Credentials> opCreds = credentials.getByVerificationCode(code);
        if (opCreds.isPresent()) {
            credentials.markAsVerified(opCreds.get());
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}