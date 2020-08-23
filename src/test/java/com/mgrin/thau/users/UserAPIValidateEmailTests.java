package com.mgrin.thau.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import com.mgrin.thau.TestUtils;
import com.mgrin.thau.credentials.CredentialService;
import com.mgrin.thau.credentials.Credentials;

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
public class UserAPIValidateEmailTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CredentialService credentialService;

    @Test
    void validateEmailNonValidCode() throws Exception {
        Mockito.when(credentialService.getByVerificationCode("123")).thenReturn(Optional.empty());

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/users/verification?code=123")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo("");

        Mockito.verify(credentialService, Mockito.never()).markAsVerified(Mockito.any());
    }

    @Test
    void validateEmailValidCode() throws Exception {
        User user = TestUtils.createUser("test");
        Credentials credentials = TestUtils.createCredentials(user);
        Mockito.when(credentialService.getByVerificationCode("123")).thenReturn(Optional.of(credentials));

        MockHttpServletResponse response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/users/verification?code=123")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo("");

        Mockito.verify(credentialService, Mockito.times(1)).markAsVerified(Mockito.any());
    }
}