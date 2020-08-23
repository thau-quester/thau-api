package com.mgrin.thau.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgrin.thau.TestUtils;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.sessions.Session;
import com.mgrin.thau.sessions.SessionAPI;
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
public class UsersAPIGetByIdTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getUserByIdUnauthorized() throws Exception {
        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/users/1").header("Content-Type", "application/json")).andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Unauthorized");
    }

    @Test
    void getUserByIdNotFound() throws Exception {
        String token = "test";
        String email = "test";

        User user = TestUtils.createUser(email);
        Session session = new Session();
        session.setId(1);
        session.setUser(user);
        session.setStrategy(Strategy.PASSWORD);

        Mockito.when(sessionService.getSessionFromToken(token)).thenReturn(Optional.of(session));
        Mockito.when(userRepository.findById(2l)).thenReturn(Optional.empty());
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.get("/users/2")
                .header("Content-Type", "application/json").header(SessionAPI.JWT_HEADER, token)).andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("User not found");
    }

    @Test
    void getUserById() throws Exception {
        String token = "test";
        String email = "test";

        User user = TestUtils.createUser(email);
        Session session = new Session();
        session.setId(1);
        session.setUser(user);
        session.setStrategy(Strategy.PASSWORD);

        Mockito.when(sessionService.getSessionFromToken(token)).thenReturn(Optional.of(session));
        Mockito.when(userRepository.findById(2l)).thenReturn(Optional.of(user));
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.get("/users/2")
                .header("Content-Type", "application/json").header(SessionAPI.JWT_HEADER, token)).andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(
                Set.of("id", "email", "username", "firstName", "lastName", "dateOfBirth", "gender", "picture"));
    }
}