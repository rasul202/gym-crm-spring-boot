package com.epam.gymcrmspringboot.bdd;

import com.epam.gymcrmspringboot.GymCrmSpringBootApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(
        classes = GymCrmSpringBootApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Import(BddTestBeans.class)
public class CucumberSpringConfiguration {
}
