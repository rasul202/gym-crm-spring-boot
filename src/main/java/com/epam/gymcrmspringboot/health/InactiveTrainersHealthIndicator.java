package com.epam.gymcrmspringboot.health;

import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component("inactiveTrainers")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class InactiveTrainersHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(InactiveTrainersHealthIndicator.class);

    TrainerRepository trainerRepository;
    InactiveTrainerHealthProperties properties;

    @Override
    public Health health() {
        int thresholdDays = properties.getThresholdDays();
        LocalDate cutoffDate = LocalDate.now().minusDays(thresholdDays);

        List<TrainerEntity> inactive;
        try {
            inactive = trainerRepository.findInactiveTrainersSince(cutoffDate);
        } catch (DataAccessException ex) {
            LOGGER.error("Failed to query inactive trainers for health check. thresholdDays={}, cutoffDate={}",
                    thresholdDays,
                    cutoffDate,
                    ex);
            return Health.down(ex)
                    .withDetail("thresholdDays", thresholdDays)
                    .withDetail("reason", "Could not query inactive trainers. Check required tables and schema state.")
                    .build();
        }

        if (inactive.isEmpty()) {
            return Health.up()
                    .withDetail("thresholdDays", thresholdDays)
                    .withDetail("inactiveTrainerCount", 0)
                    .build();
        }

        List<String> trainerDetails = inactive.stream()
                .map(t -> String.format("id=%d, username=%s, name=%s %s",
                        t.getId(),
                        t.getUser().getUsername(),
                        t.getUser().getFirstName(),
                        t.getUser().getLastName()))
                .toList();

        return Health.down()
                .withDetail("thresholdDays", thresholdDays)
                .withDetail("inactiveTrainerCount", inactive.size())
                .withDetail("inactiveTrainers", trainerDetails)
                .build();
    }
}

