package com.mgrin.thau.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgrin.thau.TestUtils;
import com.mgrin.thau.broadcaster.BroadcastingService;
import com.mgrin.thau.broadcaster.BroadcastEvent.BroadcastEventType;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.credentials.CredentialService;
import com.mgrin.thau.providers.ProviderService;
import com.mgrin.thau.sessions.Session;
import com.mgrin.thau.sessions.SessionRepository;
import com.mgrin.thau.sessions.SessionService;
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
public class UsersAPICreateUsersTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionService sessionService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BroadcastingService broadcastingService;

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private CredentialService credentialService;

    @MockBean
    private ProviderService providerService;

    private String testToken;

    @Test
    void createNewUserFailWithWrongBody() throws Exception {
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .header("Content-Type", "application/json").content("{\"test\": 123}")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Bad request");
    }

    @Test
    void createNewUserHandleExceptionThrown() throws Exception {
        Mockito.when(userRepository.findByEmail(Mockito.any())).thenThrow(new RuntimeException());
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .header("Content-Type", "application/json").content("{\"test\": 123}")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Bad request");
    }

    @Test
    void createNewUserSucceed() throws Exception {
        String email = "test@email";
        String password = "password";

        User user = TestUtils.createUser(email);
        UserInputDTO userInput = new UserInputDTO();
        userInput.setUser(user);
        userInput.setPassword(password);

        ObjectMapper objectMapper = new ObjectMapper();
        String userInputJSON = objectMapper.writeValueAsString(userInput);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> {
            Session s = i.getArgument(0);
            testToken = sessionService.createJWTToken(s);
            return s;
        });

        MockHttpServletResponse response = this.mockMvc.perform(
                MockMvcRequestBuilders.post("/users").header("Content-Type", "application/json").content(userInputJSON))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("token"));
        assertThat(body.get("token")).isEqualTo(testToken);

        Mockito.verify(broadcastingService).publish(Mockito.eq(BroadcastEventType.CREATE_NEW_USER_WITH_PASSWORD),
                Mockito.any());
        Mockito.verify(credentialService).create(Mockito.any(), Mockito.eq(password));
        Mockito.verify(providerService).create(Mockito.any(), Mockito.eq(Strategy.PASSWORD));
    }

    @Test
    void createNewUserFailIfUserExists() throws Exception {
        String email = "test@email";
        String password = "password";

        User user = TestUtils.createUser(email);
        UserInputDTO userInput = new UserInputDTO();
        userInput.setUser(user);
        userInput.setPassword(password);

        ObjectMapper objectMapper = new ObjectMapper();
        String userInputJSON = objectMapper.writeValueAsString(userInput);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> {
            Session s = i.getArgument(0);
            testToken = sessionService.createJWTToken(s);
            return s;
        });

        MockHttpServletResponse response = this.mockMvc.perform(
                MockMvcRequestBuilders.post("/users").header("Content-Type", "application/json").content(userInputJSON))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("User already registered");

        Mockito.verify(broadcastingService, Mockito.never())
                .publish(Mockito.eq(BroadcastEventType.CREATE_NEW_USER_WITH_PASSWORD), Mockito.any());
    }
}