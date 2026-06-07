package com.epam.gymcrmspringboot.metrics;

import com.epam.gymcrmspringboot.repository.TrainerRepository;
import com.epam.gymcrmspringboot.repository.TrainingRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class GymBusinessMetricsBinder implements MeterBinder {

    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;

    public GymBusinessMetricsBinder(TrainerRepository trainerRepository,
                                    TrainingRepository trainingRepository) {
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
    }

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

