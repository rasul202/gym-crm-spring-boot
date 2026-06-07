package com.epam.gymcrmspringboot.repository;

import com.epam.gymcrmspringboot.model.TraineeEntity;
import com.epam.gymcrmspringboot.model.TrainingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface TrainingRepository extends JpaRepository<TrainingEntity, Long> {

    long countByTrainingDate(LocalDate trainingDate);

    void deleteByTrainee(TraineeEntity trainee);
}
