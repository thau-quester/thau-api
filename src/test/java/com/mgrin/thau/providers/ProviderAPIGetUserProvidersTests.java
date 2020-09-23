package com.mgrin.thau.providers;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import com.mgrin.thau.TestUtils;
import com.mgrin.thau.configurations.strategies.Strategy;
import com.mgrin.thau.sessions.Session;
import com.mgrin.thau.sessions.SessionAPI;
import com.mgrin.thau.sessions.SessionRepository;
import com.mgrin.thau.sessions.SessionService;
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
public class ProviderAPIGetUserProvidersTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionService sessionService;

    @MockBean
    private ProviderRepository providerRepository;

    @MockBean
    private SessionRepository sessionRepository;

    @Test
    public void testHandlingOfNoValuesProvided() throws Exception {
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.get("/providers")).andReturn()
                .getResponse();
        HashMapConverter converter = new HashMapConverter();
        Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body.keySet()).isEqualTo(Set.of("message", "debugMessage", "status", "timestamp"));
        assertThat(body.get("message")).isEqualTo("Unauthorized");
    }

    @Test
    public void testGetProvidersForUserId() throws Exception {
        Mockito.when(providerRepository.findProvidersForUserId(1l)).thenReturn(new LinkedList<Provider>());
        MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.get("/providers?userId=1"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testGetProvidersForCurrentUser() throws Exception {
        Mockito.when(providerRepository.findProvidersForUserId(1l)).thenReturn(new LinkedList<Provider>());
        Mockito.when(sessionRepository.save(Mockito.any())).then(i -> i.getArgument(0));
        String email = "test@test.test";
        User user = TestUtils.createUser(email);

        Session testSession = sessionService.create(user, Strategy.FACEBOOK);
        testSession.setId(1);

        Mockito.when(sessionRepository.findById(1l)).thenReturn(Optional.of(testSession));

        String token = sessionService.createJWTToken(testSession);

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/providers").header(SessionAPI.JWT_HEADER, token)).andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
