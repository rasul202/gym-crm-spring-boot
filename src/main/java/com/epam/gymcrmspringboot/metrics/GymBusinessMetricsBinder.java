package com.epam.gymcrmspringboot.metrics;

import com.epam.gymcrmspringboot.repository.TrainerRepository;
import com.epam.gymcrmspringboot.repository.TrainingRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class GymBusinessMetricsBinder implements MeterBinder {

    TrainerRepository trainerRepository;
    TrainingRepository trainingRepository;

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("gymcrm.trainers.active.current", trainerRepository, TrainerRepository::countByUserIsActiveTrue)
                .description("Current number of active trainers")
                .register(registry);

        Gauge.builder("gymcrm.trainings.scheduled.today", trainingRepository,
                        repo -> repo.countByTrainingDate(LocalDate.now()))
                .description("Total number of trainings scheduled for the current day")
                .register(registry);
    }
}

