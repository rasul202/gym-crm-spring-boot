package com.epam.gymcrmspringboot.health;

import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.repository.TrainerRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component("inactiveTrainers")
public class InactiveTrainersHealthIndicator implements HealthIndicator {

    private final TrainerRepository trainerRepository;
    private final InactiveTrainerHealthProperties properties;

    public InactiveTrainersHealthIndicator(
            TrainerRepository trainerRepository,
            InactiveTrainerHealthProperties properties) {
        this.trainerRepository = trainerRepository;
        this.properties = properties;
    }

    @Override
    public Health health() {
        int thresholdDays = properties.getThresholdDays();
        LocalDate cutoffDate = LocalDate.now().minusDays(thresholdDays);

        List<TrainerEntity> inactive;
        try {
            inactive = trainerRepository.findInactiveTrainersSince(cutoffDate);
        } catch (DataAccessException ex) {
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

