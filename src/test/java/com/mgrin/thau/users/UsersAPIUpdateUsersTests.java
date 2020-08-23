package com.mgrin.thau.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class UsersAPIUpdateUsersTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void updateUserFailWithoutSessionToken() throws Exception {
        MockHttpServletResponse response = this.mockMvc
                .perform(
                        MockMvcRequestBuilders.put("/users/1").header("Content-Type", "application/json").content("{}"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Unauthorized");
    }

    @Test
    void updateUserFailWithAnotherUsersSessionToken() throws Exception {
        String token = "test";
        String email = "email";

        User user = TestUtils.createUser(email);
        user.setId(2);

        Session session = new Session();
        session.setId(1);
        session.setStrategy(Strategy.PASSWORD);
        session.setUser(user);

        Mockito.when(sessionService.getSessionFromToken(token)).thenReturn(Optional.of(session));

        ObjectMapper objectMapper = new ObjectMapper();
        String userJSON = objectMapper.writeValueAsString(user);

        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                .header("Content-Type", "application/json").header(SessionAPI.JWT_HEADER, token).content(userJSON))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Unauthorized");
    }

    @Test
    void updateUser() throws Exception {
        String token = "test";
        String email = "email";

        User user = TestUtils.createUser(email);
        user.setId(1);
        Session session = new Session();
        session.setId(1);
        session.setStrategy(Strategy.PASSWORD);
        session.setUser(user);

        User userUpdate = TestUtils.createUser(email);
        userUpdate.setEmail("trololo@tro.lolo");
        userUpdate.setFirstName("New first name!");

        Mockito.when(sessionService.getSessionFromToken(token)).thenReturn(Optional.of(session));

        ObjectMapper objectMapper = new ObjectMapper();
        String userUpdateJSON = objectMapper.writeValueAsString(userUpdate);

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.put("/users/1").header("Content-Type", "application/json")
                        .header(SessionAPI.JWT_HEADER, token).content(userUpdateJSON))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isNotEqualTo("");

        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
        assertThat(body.get("email")).isEqualTo(email);
        assertThat(body.get("firstName")).isEqualTo("New first name!");
        Mockito.verify(userRepository).save(Mockito.any());
    }
}