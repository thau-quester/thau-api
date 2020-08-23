package com.mgrin.thau.sessions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgrin.thau.broadcaster.BroadcastingService;
import com.mgrin.thau.broadcaster.BroadcastEvent.BroadcastEventType;
import com.mgrin.thau.credentials.CredentialService;
import com.mgrin.thau.users.User;
import com.mgrin.thau.users.UserService;
import com.mgrin.thau.utils.HashMapConverter;
import com.restfb.exception.FacebookOAuthException;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
public class SessionsAPICreateSessionWithFacebookTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionService sessionService;

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private CredentialService credentialService;

    @MockBean
    private FacebookService facebookService;

    @MockBean
    private UserService userService;

    @Mock
    FacebookOAuthException facebookException;

    @MockBean
    private BroadcastingService broadcastingService;

    private String testToken;

    @Test
    void createSessionFailWithWrongBody() throws Exception {
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.post("/session/facebook")
                .header("Content-Type", "application/json").content("{\"test\": 123}")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("User not found");
    }

    @Test
    void createSessionHandlesException() throws Exception {
        Mockito.when(facebookService.getFacebookUser("123")).thenThrow(facebookException);
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.post("/session/facebook")
                .header("Content-Type", "application/json").content("{\"accessToken\": \"123\"}")).andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Unauthorized");
    }

    @Test
    void createSessionForNewUser() throws Exception {
        String email = "new@user";
        com.restfb.types.User fbUser = new com.restfb.types.User();
        fbUser.setEmail(email);
        Mockito.when(facebookService.getFacebookUser("123")).thenReturn(fbUser);
        Mockito.when(userService.getByEmail(email)).thenReturn(Optional.empty());
        Mockito.when(userService.create(Mockito.any(), Mockito.eq(fbUser))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> {
            Session s = i.getArgument(0);
            testToken = sessionService.createJWTToken(s);
            return s;
        });

        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.post("/session/facebook")
                .header("Content-Type", "application/json").content("{\"accessToken\": \"123\"}")).andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("token"));
        assertThat(body.get("token")).isEqualTo(testToken);
        Mockito.verify(broadcastingService)
                .publish(Mockito.eq(BroadcastEventType.EXCHANGE_FACEBOOK_AUTH_TOKEN_FOR_TOKEN), Mockito.any());
    }

    @Test
    void createSessionForExistingUser() throws Exception {
        String email = "new@user";
        com.restfb.types.User fbUser = new com.restfb.types.User();
        fbUser.setEmail(email);
        User user = User.of(fbUser);

        Mockito.when(facebookService.getFacebookUser("123")).thenReturn(fbUser);
        Mockito.when(userService.getByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> {
            Session s = i.getArgument(0);
            testToken = sessionService.createJWTToken(s);
            return s;
        });

        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.post("/session/facebook")
                .header("Content-Type", "application/json").content("{\"accessToken\": \"123\"}")).andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("token"));

        Mockito.verify(userService, Mockito.times(1)).updateProvidersData(user, fbUser);
        Mockito.verify(broadcastingService)
                .publish(Mockito.eq(BroadcastEventType.EXCHANGE_FACEBOOK_AUTH_TOKEN_FOR_TOKEN), Mockito.any());
    }
}