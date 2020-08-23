package com.mgrin.thau;

import com.mgrin.thau.configurations.ThauConfigurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private ThauConfigurations configurations;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (configurations.isCORSEnabled()) {
            registry.addMapping("/**").allowedMethods("*");
        }
    }
}
