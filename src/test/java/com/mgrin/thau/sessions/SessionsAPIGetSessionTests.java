package com.mgrin.thau.sessions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgrin.thau.TestUtils;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.credentials.CredentialService;
import com.mgrin.thau.credentials.Credentials;
import com.mgrin.thau.users.User;
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
public class SessionsAPIGetSessionTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionRepository sessionRepository;

    @Autowired
    private SessionService sessionService;

    @MockBean
    private CredentialService credentialService;

    @Test
    void getCurrentSessionWithoutTokenTest() throws Exception {
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.get("/session")).andReturn()
                .getResponse();
        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Unauthorized");
    }

    @Test
    void getCurrentSessionWithWrongTokenTest() throws Exception {
        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/session").header(SessionAPI.JWT_HEADER, "test")).andReturn()
                .getResponse();
        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Unauthorized");
    }

    @Test
    void getCurrentSessionWithWrongSessionIdTest() throws Exception {
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));

        String email = "test@test.test";
        User user = TestUtils.createUser(email);

        Session testSession = sessionService.create(user, Strategy.FACEBOOK);
        testSession.setId(1);

        Mockito.when(sessionRepository.findById(1l)).thenReturn(Optional.empty());
        String token = sessionService.createJWTToken(testSession);

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/session").header(SessionAPI.JWT_HEADER, token)).andReturn()
                .getResponse();
        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Unauthorized");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCurrentSessionWithFacebookStrategyTest() throws Exception {
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));
        String email = "test@test.test";
        User user = TestUtils.createUser(email);

        Session testSession = sessionService.create(user, Strategy.FACEBOOK);
        testSession.setId(1);

        Mockito.when(sessionRepository.findById(1l)).thenReturn(Optional.of(testSession));

        String token = sessionService.createJWTToken(testSession);

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/session").header(SessionAPI.JWT_HEADER, token)).andReturn()
                .getResponse();
        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body.keySet()).isEqualTo(Set.of("id", "user", "strategy"));
        assertThat(body.get("id")).isEqualTo(1);
        Map<String, Object> receivedUser = (Map<String, Object>) body.get("user");
        assertThat(receivedUser.get("id")).isEqualTo(1);
        assertThat(body.get("strategy")).isEqualTo(Strategy.FACEBOOK.getValue());
    }

    @Test
    void getCurrentSessionWithPasswordWithoutCredentialsStrategyTest() throws Exception {
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));
        String email = "test@test.test";
        User user = TestUtils.createUser(email);

        Session testSession = sessionService.create(user, Strategy.PASSWORD);
        testSession.setId(1);

        Mockito.when(sessionRepository.findById(1l)).thenReturn(Optional.of(testSession));

        String token = sessionService.createJWTToken(testSession);

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/session").header(SessionAPI.JWT_HEADER, token)).andReturn()
                .getResponse();
        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("User does not have password credentials");
    }

    @Test
    void getCurrentSessionWithPasswordStrategyEmailNotValidatedTest() throws Exception {
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));
        String email = "test@test.test";
        User user = TestUtils.createUser(email);

        Session testSession = sessionService.create(user, Strategy.PASSWORD);
        testSession.setId(1);

        Credentials credentials = TestUtils.createCredentials(user);

        Mockito.when(sessionRepository.findById(1l)).thenReturn(Optional.of(testSession));
        Mockito.when(credentialService.getByEmail(user.getEmail())).thenReturn(Optional.of(credentials));

        String token = sessionService.createJWTToken(testSession);

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/session").header(SessionAPI.JWT_HEADER, token)).andReturn()
                .getResponse();
        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Email is not yet validated");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCurrentSessionWithPasswordStrategyTest() throws Exception {
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));
        String email = "test@test.test";
        User user = TestUtils.createUser(email);

        Session testSession = sessionService.create(user, Strategy.PASSWORD);
        testSession.setId(1);

        Credentials credentials = TestUtils.createCredentials(user, true);

        Mockito.when(sessionRepository.findById(1l)).thenReturn(Optional.of(testSession));
        Mockito.when(credentialService.getByEmail(user.getEmail())).thenReturn(Optional.of(credentials));

        String token = sessionService.createJWTToken(testSession);

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/session").header(SessionAPI.JWT_HEADER, token)).andReturn()
                .getResponse();
        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body.keySet()).isEqualTo(Set.of("id", "user", "strategy"));
        assertThat(body.get("id")).isEqualTo(1);
        Map<String, Object> receivedUser = (Map<String, Object>) body.get("user");
        assertThat(receivedUser.get("id")).isEqualTo(1);
        assertThat(body.get("strategy")).isEqualTo(Strategy.PASSWORD.getValue());
    }
}