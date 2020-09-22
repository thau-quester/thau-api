package com.mgrin.thau.sessions;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

import com.mgrin.thau.TestUtils;
import com.mgrin.thau.configurations.strategies.Strategy;
import com.mgrin.thau.users.User;

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
public class SessionsAPILogoutTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionRepository sessionRepository;

    @Autowired
    private SessionService sessionService;

    @Test
    public void testLogoutFromCurrentSession() throws Exception {
        String email = "test@test.test";
        User user = TestUtils.createUser(email);

        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));

        Session currentSession = sessionService.create(user, Strategy.GOOGLE);
        currentSession.setId(1);
        Mockito.when(sessionRepository.findById(1l)).thenReturn(Optional.of(currentSession));

        String token = sessionService.createJWTToken(currentSession);
        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.delete("/session").header(SessionAPI.JWT_HEADER, token)).andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(currentSession.isOpen()).isFalse();
    }

    @Test
    public void testLogoutFromAnotherActiveSession() throws Exception {
        String email = "test@test.test";
        User user = TestUtils.createUser(email);

        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));

        Session currentSession = sessionService.create(user, Strategy.GOOGLE);
        currentSession.setId(1);
        Session anotherSession = sessionService.create(user, Strategy.FACEBOOK);
        anotherSession.setId(2);
        Mockito.when(sessionRepository.findById(1l)).thenReturn(Optional.of(currentSession));
        Mockito.when(sessionRepository.findById(2l)).thenReturn(Optional.of(anotherSession));

        String token = sessionService.createJWTToken(currentSession);
        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.delete("/session?sessionId=2").header(SessionAPI.JWT_HEADER, token))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(currentSession.isOpen()).isTrue();
        assertThat(anotherSession.isOpen()).isFalse();
    }

    @Test
    public void testFailToCloseNotYourSession() throws Exception {
        String email = "test@test.test";
        User user = TestUtils.createUser(email);
        String email2 = "test2@test.test";
        User user2 = TestUtils.createUser(email2);
        user2.setId(2l);

        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));

        Session currentSession = sessionService.create(user, Strategy.GOOGLE);
        currentSession.setId(1);
        Session anotherUserSession = sessionService.create(user2, Strategy.FACEBOOK);
        anotherUserSession.setId(2);
        Mockito.when(sessionRepository.findById(1l)).thenReturn(Optional.of(currentSession));
        Mockito.when(sessionRepository.findById(2l)).thenReturn(Optional.of(anotherUserSession));

        String token = sessionService.createJWTToken(currentSession);

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.delete("/session?sessionId=2").header(SessionAPI.JWT_HEADER, token))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(currentSession.isOpen()).isTrue();
        assertThat(anotherUserSession.isOpen()).isTrue();
    }
}
