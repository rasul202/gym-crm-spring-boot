package com.epam.gymcrmspringboot.config;

import com.epam.gymcrmspringboot.decoder.WorkloadClientErrorDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkloadClientFeignConfig {

    @Bean
    public ErrorDecoder workloadClientErrorDecoder(ObjectMapper objectMapper) {
        return new WorkloadClientErrorDecoder(objectMapper);
    }
}
