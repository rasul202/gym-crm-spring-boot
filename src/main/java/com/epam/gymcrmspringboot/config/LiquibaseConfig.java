package com.epam.gymcrmspringboot.config;

import javax.sql.DataSource;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class LiquibaseConfig {

    @Bean
    public SpringLiquibase springLiquibase(
            DataSource dataSource,
            @Value("${spring.liquibase.change-log:classpath:liquibase/changelog-master.yml}") String changeLog,
            @Value("${spring.liquibase.enabled:true}") boolean enabled) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(enabled);
        return liquibase;
    }

    @Bean
    public static BeanFactoryPostProcessor liquibaseEntityManagerFactoryDependencyPostProcessor() {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
                BeanDefinition entityManagerFactory = beanFactory.getBeanDefinition("entityManagerFactory");
                String[] dependsOn = entityManagerFactory.getDependsOn();
                entityManagerFactory.setDependsOn(StringUtils.addStringToArray(dependsOn, "springLiquibase"));
            }
        };
    }
}

