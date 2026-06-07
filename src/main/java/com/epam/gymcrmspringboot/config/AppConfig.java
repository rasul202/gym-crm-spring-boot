package com.epam.gymcrmspringboot.config;

import com.epam.gymcrmspringboot.logging.RestCallLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class AppConfig {

    @Bean
    public FilterRegistrationBean<RestCallLoggingFilter> restCallLoggingFilterRegistration() {
        FilterRegistrationBean<RestCallLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RestCallLoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}

