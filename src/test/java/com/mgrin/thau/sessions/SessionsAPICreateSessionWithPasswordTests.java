package com.mgrin.thau.sessions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgrin.thau.TestUtils;
import com.mgrin.thau.broadcaster.BroadcastingService;
import com.mgrin.thau.broadcaster.BroadcastEvent.BroadcastEventType;
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
public class SessionsAPICreateSessionWithPasswordTests {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private SessionRepository sessionRepository;

        @MockBean
        private CredentialService credentialService;

        @MockBean
        private BroadcastingService broadcastingService;

        @Test
        void createSessionWithWrongBody() throws Exception {
                MockHttpServletResponse response = this.mockMvc
                                .perform(MockMvcRequestBuilders.post("/session/password")
                                                .header("Content-Type", "application/json").content("{\"test\": 123}"))
                                .andReturn().getResponse();
                assertThat(response.getStatus()).isEqualTo(404);
                assertThat(response.getContentAsString()).isNotEqualTo("");

                HashMapConverter converter = new HashMapConverter();
                Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
                assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
                assertThat(body.get("message")).isEqualTo("User not found");
        }

        @Test
        void createSessionWithWrongEmail() throws Exception {
                Mockito.when(credentialService.getByEmailAndPassword("wrong@email", "valid password"))
                                .thenReturn(Optional.empty());

                String passwordAuth = "{ \"email\": \"wrong@email\", \"password\": \"valid password\" }";
                MockHttpServletResponse response = this.mockMvc
                                .perform(MockMvcRequestBuilders.post("/session/password")
                                                .header("Content-Type", "application/json").content(passwordAuth))
                                .andReturn().getResponse();
                assertThat(response.getStatus()).isEqualTo(404);
                assertThat(response.getContentAsString()).isNotEqualTo("");

                HashMapConverter converter = new HashMapConverter();
                Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
                assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
                assertThat(body.get("message")).isEqualTo("User not found");
        }

        @Test
        void createSessionWithWrongPassword() throws Exception {
                Mockito.when(credentialService.getByEmailAndPassword("valid@email", "wrong password"))
                                .thenReturn(Optional.empty());

                String passwordAuth = "{ \"email\": \"valid@email\", \"password\": \"wrong password\" }";
                MockHttpServletResponse response = this.mockMvc
                                .perform(MockMvcRequestBuilders.post("/session/password")
                                                .header("Content-Type", "application/json").content(passwordAuth))
                                .andReturn().getResponse();
                assertThat(response.getStatus()).isEqualTo(404);
                assertThat(response.getContentAsString()).isNotEqualTo("");

                HashMapConverter converter = new HashMapConverter();
                Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
                assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
                assertThat(body.get("message")).isEqualTo("User not found");
        }

        @Test
        void createSessionWithValidPassword() throws Exception {
                String email = "valid@email";
                String password = "valid password";

                User user = TestUtils.createUser(email);
                Credentials testCredentials = TestUtils.createCredentials(user);

                Mockito.when(credentialService.getByEmailAndPassword(email, password))
                                .thenReturn(Optional.of(testCredentials));
                Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));

                String passwordAuth = "{ \"email\": \"valid@email\", \"password\": \"valid password\" }";
                MockHttpServletResponse response = this.mockMvc
                                .perform(MockMvcRequestBuilders.post("/session/password")
                                                .header("Content-Type", "application/json").content(passwordAuth))
                                .andReturn().getResponse();
                assertThat(response.getStatus()).isEqualTo(200);
                assertThat(response.getContentAsString()).isNotEqualTo("");

                HashMapConverter converter = new HashMapConverter();
                Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
                assertThat(body.keySet()).isEqualTo(Set.of("token"));

                Mockito.verify(broadcastingService).publish(Mockito.eq(BroadcastEventType.EXCHANGE_PASSWORD_FOR_TOKEN),
                                Mockito.any());
        }
}