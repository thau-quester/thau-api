package com.mgrin.thau.providers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.mgrin.thau.TestUtils;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.users.User;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ProviderServiceTests {

    @MockBean
    private ProviderRepository providerRepository;

    @Autowired
    private ProviderService providerService;

    @Test
    void contextLoads() {
        assertThat(providerService).isNotNull();
    }

    @Test
    void createTest() {
        String email = "test@test.test";

        User user = TestUtils.createUser(email);

        Mockito.when(providerRepository.save(Mockito.any())).then(i -> i.getArgument(0));
        Provider providerWithoutAdditionalData = providerService.create(user, Strategy.PASSWORD);

        Map<String, Object> data = new HashMap<>();
        data.put("test", "test");
        Provider providerWithAdditionalData = providerService.create(user, Strategy.PASSWORD, data);

        assertThat(providerWithoutAdditionalData.getUser()).isEqualTo(user);
        assertThat(providerWithAdditionalData.getUser()).isEqualTo(user);
        assertThat(providerWithoutAdditionalData.getProvider()).isEqualTo(Strategy.PASSWORD);
        assertThat(providerWithAdditionalData.getProvider()).isEqualTo(Strategy.PASSWORD);
        assertThat(providerWithoutAdditionalData.getData()).isEqualTo(null);
        assertThat(providerWithAdditionalData.getData()).isEqualTo(data);
    }
}