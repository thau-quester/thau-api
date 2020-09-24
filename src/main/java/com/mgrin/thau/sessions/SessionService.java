package com.mgrin.thau.sessions;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgrin.thau.configurations.ThauConfigurations;
import com.mgrin.thau.configurations.strategies.Strategy;
import com.mgrin.thau.permissions.PermissionService;
import com.mgrin.thau.users.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

@Service
@Transactional
public class SessionService {

    public static final String JWT_ISSUER = "thau";

    private SessionRepository sessionRepository;

    private PermissionService permissionService;
    private ThauConfigurations configurations;

    @Autowired
    public SessionService(SessionRepository sessionRepository, PermissionService permissionService,
            ThauConfigurations configurations) {
        this.sessionRepository = sessionRepository;
        this.configurations = configurations;
        this.permissionService = permissionService;
    }

    public Optional<Session> getById(long id) {
        return sessionRepository.findById(id);
    }

    public Session create(User user, Strategy strategy) {
        Session session = new Session();
        session.setUser(user);
        session.setStrategy(strategy);

        session = sessionRepository.save(session);
        return session;
    }

    public String createJWTToken(Session session) {
        Algorithm algorithm = configurations.getJWTConfiguration().getAlgorithm();
        String token = JWT.create().withAudience(configurations.getAppName()).withIssuer(JWT_ISSUER)
                .withExpiresAt(
                        new Date(System.currentTimeMillis() + configurations.getJWTConfiguration().getTokenLifetime()))
                .withIssuedAt(new Date()).withClaim("session_id", session.getId())
                .withClaim("user_id", session.getUser().getId()).sign(algorithm);

        return token;
    }

    public Optional<Session> getSessionFromToken(String token) throws JsonMappingException, JsonProcessingException {
        if (token == null) {
            return Optional.empty();
        }

        Algorithm algorithm = configurations.getJWTConfiguration().getAlgorithm();
        JWTVerifier verifier = JWT.require(algorithm).withAudience(configurations.getAppName()).withIssuer(JWT_ISSUER)
                .build();
        DecodedJWT jwt = verifier.verify(token);

        String payloadBase64 = jwt.getPayload();
        String payload = new String(Base64Utils.decodeFromString(payloadBase64));

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
        };
        Map<String, String> map = mapper.readValue(payload, typeRef);

        Optional<Session> opSession = this.getById(Long.valueOf(map.get("session_id")));
        if (opSession.isPresent() && !opSession.get().isOpen()) {
            return Optional.empty();
        }
        opSession.get().setRoles(permissionService.getUserRoles(opSession.get().getUser()));
        return opSession;
    }

    public Session closeSession(Session session) {
        session.setOpen(false);
        return sessionRepository.save(session);
    }

    public Collection<Session> getOpenSession(long userId) {
        return sessionRepository.findOpenSessionsForUserId(userId);
    }
}