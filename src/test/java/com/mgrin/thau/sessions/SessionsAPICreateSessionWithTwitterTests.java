package com.mgrin.thau.sessions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgrin.thau.broadcaster.BroadcastingService;
import com.mgrin.thau.broadcaster.BroadcastEvent.BroadcastEventType;
import com.mgrin.thau.credentials.CredentialService;
import com.mgrin.thau.sessions.externalServices.TwitterService;
import com.mgrin.thau.users.User;
import com.mgrin.thau.users.UserService;
import com.mgrin.thau.utils.HashMapConverter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class SessionsAPICreateSessionWithTwitterTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionService sessionService;

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private CredentialService credentialService;

    @MockBean
    private TwitterService twitterService;

    @MockBean
    private UserService userService;

    @MockBean
    private BroadcastingService broadcastingService;

    private String testToken;

    @Test
    void createSessionFailWithWrongBody() throws Exception {
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.post("/session/twitter")
                .header("Content-Type", "application/json").content("{\"test\": 123}")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("User not found");
    }

    @Test
    void retrieveTwitterRedirectURI() throws Exception {
        Mockito.when(twitterService.getTwitterRedirectURI("localhost")).thenReturn("twitter_redirect");
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.post("/session/twitter")
                .header("Content-Type", "application/json").content("{\"redirectURI\": \"localhost\"}")).andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("twitter_redirect");
    }

    @Test
    void createSessionForNewUser() throws Exception {
        twitter4j.User twitterUser = Mockito.mock(twitter4j.User.class);
        Mockito.when(twitterUser.getEmail()).thenReturn(null);
        Mockito.when(twitterUser.getScreenName()).thenReturn("test");

        Mockito.when(twitterService.getTwitterUser("token", "verifier")).thenReturn(twitterUser);
        Mockito.when(userService.getByEmail("test@twitter.thau")).thenReturn(Optional.empty());
        Mockito.when(userService.create(Mockito.any(), Mockito.eq(twitterUser))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> {
            Session s = i.getArgument(0);
            testToken = sessionService.createJWTToken(s);
            return s;
        });

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/session/twitter").header("Content-Type", "application/json")
                        .content("{\"oauth_token\": \"token\", \"oauth_verifier\": \"verifier\"}"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("token"));
        assertThat(body.get("token")).isEqualTo(testToken);
        Mockito.verify(broadcastingService)
                .publish(Mockito.eq(BroadcastEventType.EXCHANGE_TWITTER_AUTH_TOKEN_FOR_TOKEN), Mockito.any());
    }

    @Test
    void createSessionForExistingUser() throws Exception {
        twitter4j.User twitterUser = Mockito.mock(twitter4j.User.class);
        Mockito.when(twitterUser.getEmail()).thenReturn(null);
        Mockito.when(twitterUser.getScreenName()).thenReturn("test");
        User user = User.of(twitterUser);

        Mockito.when(twitterService.getTwitterUser("token", "verifier")).thenReturn(twitterUser);
        Mockito.when(userService.getByEmail("test@twitter.thau")).thenReturn(Optional.of(user));
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> {
            Session s = i.getArgument(0);
            testToken = sessionService.createJWTToken(s);
            return s;
        });

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/session/twitter").header("Content-Type", "application/json")
                        .content("{\"oauth_token\": \"token\", \"oauth_verifier\": \"verifier\"}"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("token"));

        Mockito.verify(userService, Mockito.times(1)).updateProvidersData(user, twitterUser);
        Mockito.verify(broadcastingService)
                .publish(Mockito.eq(BroadcastEventType.EXCHANGE_TWITTER_AUTH_TOKEN_FOR_TOKEN), Mockito.any());
    }
}