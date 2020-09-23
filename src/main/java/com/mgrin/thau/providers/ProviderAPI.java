package com.mgrin.thau.providers;

import java.util.Collection;
import java.util.Optional;

import com.mgrin.thau.APIError;
import com.mgrin.thau.sessions.Session;
import com.mgrin.thau.sessions.SessionAPI;
import com.mgrin.thau.sessions.SessionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/providers")
public class ProviderAPI {

    private SessionService sessionService;

    private ProviderRepository providers;

    @Autowired
    public ProviderAPI(ProviderRepository providers, SessionService sessionService) {
        this.providers = providers;
        this.sessionService = sessionService;
    }

    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public Collection<Provider> getUserProviders(@RequestParam(required = false) Long userId,
            @RequestHeader(name = SessionAPI.JWT_HEADER, required = false) String token) {
        Long requestedUserId = userId;
        if (token == null && userId == null) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        if (requestedUserId == null) {
            Optional<Session> opSession;
            try {
                opSession = sessionService.getSessionFromToken(token);
            } catch (Exception e) {
                throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized: could not decrypt token", e);
            }

            if (!opSession.isPresent()) {
                throw new APIError(HttpStatus.UNAUTHORIZED, "Unauthorized: no session found");
            }

            requestedUserId = opSession.get().getUser().getId();
        }

        Collection<Provider> userProviders = providers.findProvidersForUserId(requestedUserId);
        return userProviders;
    }
}
