package com.mgrin.thau.sessions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgrin.thau.TestUtils;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.credentials.CredentialService;
import com.mgrin.thau.credentials.Credentials;
import com.mgrin.thau.users.User;
import com.mgrin.thau.utils.HashMapConverter;

import org.json.JSONArray;
import org.json.JSONObject;
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
        assertThat(body.get("message")).isEqualTo("Unauthorized: no token provided");
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
        assertThat(body.get("message")).isEqualTo("Unauthorized: could not decrypt token");
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
        assertThat(body.get("message")).isEqualTo("Unauthorized: no session found");
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
        assertThat(body.keySet()).isEqualTo(Set.of("id", "user", "strategy", "open"));
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
        assertThat(body.keySet()).isEqualTo(Set.of("id", "user", "strategy", "open"));
        assertThat(body.get("id")).isEqualTo(1);
        Map<String, Object> receivedUser = (Map<String, Object>) body.get("user");
        assertThat(receivedUser.get("id")).isEqualTo(1);
        assertThat(body.get("strategy")).isEqualTo(Strategy.PASSWORD.getValue());
    }

    @Test
    public void getOpenSessionsTest() throws Exception {
        String email = "test@test.test";
        User user = TestUtils.createUser(email);

        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));

        Session currentSession = sessionService.create(user, Strategy.GOOGLE);
        currentSession.setId(1);

        Session anotherActiveSession = sessionService.create(user, Strategy.FACEBOOK);
        anotherActiveSession.setId(2);

        Session closedSession = sessionService.create(user, Strategy.GITHUB);
        closedSession.setId(3);
        closedSession.setOpen(false);

        Mockito.when(sessionRepository.findById(1l)).thenReturn(Optional.of(currentSession));
        Mockito.when(sessionRepository.findById(2l)).thenReturn(Optional.of(anotherActiveSession));
        Mockito.when(sessionRepository.findById(3l)).thenReturn(Optional.of(closedSession));

        LinkedList<Session> openSessions = new LinkedList<>();
        openSessions.add(currentSession);
        openSessions.add(anotherActiveSession);
        Mockito.when(sessionRepository.findOpenSessionsForUserId(user.getId())).thenReturn(openSessions);

        String token = sessionService.createJWTToken(currentSession);

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/session/open").header(SessionAPI.JWT_HEADER, token)).andReturn()
                .getResponse();
        JSONArray body = new JSONArray(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body.length()).isEqualTo(2);
        assertThat(body.getJSONObject(0).keySet()).isEqualTo(Set.of("id", "user", "strategy", "open"));
        assertThat(body.getJSONObject(1).keySet()).isEqualTo(Set.of("id", "user", "strategy", "open"));
        assertThat(body.getJSONObject(0).get("id")).isEqualTo(1);
        assertThat(body.getJSONObject(1).get("id")).isEqualTo(2);
        assertThat(body.getJSONObject(0).get("open")).isEqualTo(true);
        assertThat(body.getJSONObject(1).get("open")).isEqualTo(true);
        assertThat(body.getJSONObject(0).get("strategy")).isEqualTo(Strategy.GOOGLE.getValue());
        assertThat(body.getJSONObject(1).get("strategy")).isEqualTo(Strategy.FACEBOOK.getValue());
        assertThat(body.getJSONObject(0).get("user")).isEqualTo(JSONObject.NULL);
        assertThat(body.getJSONObject(1).get("user")).isEqualTo(JSONObject.NULL);
    }
}