package com.epam.gymcrmspringboot.health;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "health.inactive-trainer")
public class InactiveTrainerHealthProperties {

    @Min(1)
    private int thresholdDays = 30;
}

