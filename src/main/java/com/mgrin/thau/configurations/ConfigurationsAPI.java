package com.mgrin.thau.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configs")
public class ConfigurationsAPI {

    private ThauConfigurations configurations;

    @Autowired
    public ConfigurationsAPI(ThauConfigurations configurations) {
        this.configurations = configurations;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public PublicConfigurationDTO getPublicConfiguration() {
        return new PublicConfigurationDTO(configurations);
    }
}