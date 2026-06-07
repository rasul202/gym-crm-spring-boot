package com.epam.gymcrmspringboot.config;

import com.epam.gymcrmspringboot.logging.TransactionCorrelationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TransactionCorrelationInterceptor transactionCorrelationInterceptor;

    public WebMvcConfig(TransactionCorrelationInterceptor transactionCorrelationInterceptor) {
        this.transactionCorrelationInterceptor = transactionCorrelationInterceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(transactionCorrelationInterceptor);
    }
}
