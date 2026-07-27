package com.epam.gymcrmspringboot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {

    /**
     * Empty by default: no browser cross-origin access is allowed until explicitly configured.
     */
    private List<String> allowedOrigins = new ArrayList<>();

    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "X-Requested-With");

    private boolean allowCredentials;

    private Long maxAgeSeconds = 3600L;
}

