package com.epam.gymcrmspringboot.repository;

import com.epam.gymcrmspringboot.model.TrainingTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingTypeRepository extends JpaRepository<TrainingTypeEntity, Long> {

    Optional<TrainingTypeEntity> findByTrainingTypeNameIgnoreCase(String trainingTypeName);

}
