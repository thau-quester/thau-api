package com.mgrin.thau.configurations;

import com.mgrin.thau.configurations.broadcast.BroadcastChannel;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.utils.HashMapConverter;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ConfigurationsTests {

        @Autowired
        private ThauConfigurations configurations;

        @Autowired
        private MockMvc mockMvc;

        @Test
        void contextLoads() throws MalformedURLException {
                assertThat(configurations).isNotNull();

                assertThat(configurations.getEnvironment()).isEqualTo("TEST");
                assertThat(configurations.getAppName()).isEqualTo("thau-test");
                assertThat(configurations.getAvailableStrategies())
                                .isEqualTo(List.of(Strategy.GOOGLE, Strategy.FACEBOOK, Strategy.PASSWORD,
                                                Strategy.GITHUB, Strategy.TWITTER, Strategy.LINKEDIN));
                assertThat(configurations.getBroadcastChannels()).isEqualTo(List.of(BroadcastChannel.HTTP));

                assertThat(configurations.getJWTConfiguration()).isNotNull();
                assertThat(configurations.getJWTConfiguration().getEncryptionAlgorithm()).isEqualTo("HMAC");
                assertThat(configurations.getJWTConfiguration().getTokenLifetime()).isEqualTo(864000000);

                assertThat(configurations.getPasswordStrategyConfiguration()).isNotNull();
                assertThat(configurations.getPasswordStrategyConfiguration().isRequireEmailVerification()).isTrue();

                assertThat(configurations.getFacebookStrategyConfiguration()).isNotNull();
                assertThat(configurations.getFacebookStrategyConfiguration().getGraphVersion()).isEqualTo("v7.0");

                assertThat(configurations.getGoogleStrategyConfiguration()).isNotNull();

                assertThat(configurations.getHttpBroadcastConfiguration()).isNotNull();
                assertThat(configurations.getHttpBroadcastConfiguration().getUrl())
                                .isEqualTo(new URL("http://google.com/"));
        }

        @Test
        void configurationsEndpointShouldReturnPredefinedShape() throws Exception {
                MockHttpServletResponse response = this.mockMvc.perform(MockMvcRequestBuilders.get("/configs"))
                                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse();
                HashMapConverter converter = new HashMapConverter();
                Map<String, Object> body = converter.convertToEntityAttribute(response.getContentAsString());
                assertThat(body.get("appName")).isEqualTo("thau-test");
                assertThat(body.get("environment")).isEqualTo("TEST");
                assertThat(body.get("broadcastChannels")).isEqualTo(List.of("http"));
                assertThat(body.get("availableStrategies"))
                                .isEqualTo(List.of("google", "facebook", "password", "github", "twitter", "linkedin"));
                assertThat(body.keySet()).isEqualTo(Set.of("apiVersion", "environment", "appName",
                                "passwordStrategyConfiguration", "googleStrategyConfiguration",
                                "facebookStrategyConfiguration", "gitHubStrategyConfiguration",
                                "twitterStrategyConfiguration", "availableStrategies", "linkedinStrategyConfiguration",
                                "broadcastChannels", "jwtconfiguration", "corsenabled"));
        }
}
