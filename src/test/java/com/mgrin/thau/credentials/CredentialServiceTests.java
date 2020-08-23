package com.mgrin.thau.credentials;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import com.mgrin.thau.TestUtils;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.users.User;

import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class CredentialServiceTests {

    @MockBean
    private CredentialRepository credentialRepository;

    @Autowired
    private CredentialService credentialService;

    @Test
    void contextLoads() {
        assertThat(credentialService).isNotNull();
    }

    private Credentials createCredentials(String email, String password) {
        User user = TestUtils.createUser(email);
        Mockito.when(credentialRepository.save(Mockito.any())).then(i -> i.getArgument(0));
        Credentials creds = credentialService.create(user, password);
        return creds;
    }

    @Test
    void createCredentialForUserWithPasswordTest() {
        String email = "test@test.test";
        String password = "1234";
        Credentials creds = createCredentials(email, password);

        Mockito.verify(credentialRepository).save(Mockito.any());
        assertThat(creds).isNotNull();
        assertThat(creds.getEmail()).isEqualTo(email);
        assertThat(creds.getStrategy()).isEqualTo(Strategy.PASSWORD);
        assertThat(creds.getVerificationCode()).isNotNull();
        assertThat(creds.getSalt()).isNotNull();
        assertThat(creds.getPassword()).isEqualTo(credentialService.getHash(password, creds.getSalt()));
    }

    @Test
    void markAsVerifiedTest() {
        String email = "test@test.test";
        String password = "1234";
        Credentials creds = createCredentials(email, password);

        credentialService.markAsVerified(creds);
        assertThat(creds.isVerified()).isTrue();
    }

    @Test
    void getByEmailAndPasswordTest() {
        String email = "test@test.test";
        String password = "1234";
        Credentials creds = createCredentials(email, password);

        Mockito.when(credentialRepository.findByEmail(email)).thenReturn(Optional.of(creds));
        Mockito.when(credentialRepository.findByEmail(AdditionalMatchers.not(ArgumentMatchers.eq(email))))
                .thenReturn(Optional.empty());

        Optional<Credentials> wrongEmail = credentialService.getByEmailAndPassword("wrong", password);
        assertThat(wrongEmail.isEmpty()).isTrue();

        Optional<Credentials> wrongPassword = credentialService.getByEmailAndPassword(email, "wrong");
        assertThat(wrongPassword.isEmpty()).isTrue();

        Optional<Credentials> allGood = credentialService.getByEmailAndPassword(email, password);
        assertThat(allGood.isEmpty()).isFalse();
        assertThat(allGood.get()).isEqualTo(creds);
    }
}