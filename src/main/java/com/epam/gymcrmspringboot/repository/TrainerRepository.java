package com.epam.gymcrmspringboot.repository;

import com.epam.gymcrmspringboot.model.TrainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<TrainerEntity, Long> {

    long countByUserIsActiveTrue();

    Optional<TrainerEntity> findByUserUsername(String username);

    List<TrainerEntity> findByUserUsernameInAndUserIsActiveTrue(List<String> usernames);

    @Query("""
            SELECT tr
            FROM TrainerEntity tr
            WHERE tr.user.isActive = true
              AND EXISTS (
                  SELECT 1
                  FROM TraineeEntity te
                  WHERE te.user.username = :traineeUsername
              )
              AND NOT EXISTS (
                  SELECT 1
                   FROM TrainingEntity t
                   WHERE t.trainee.user.username = :traineeUsername
                     AND t.trainer.id = tr.id
              )
            """)
    List<TrainerEntity> findAvailableTrainersForTrainee(@Param("traineeUsername") String traineeUsername);

    @Query("""
            SELECT tr
            FROM TrainerEntity tr
            JOIN FETCH tr.user u
            WHERE u.isActive = true
              AND NOT EXISTS (
                  SELECT 1
                  FROM TrainingEntity t
                  WHERE t.trainer = tr
                    AND t.trainingDate >= :cutoffDate
              )
            """)
    List<TrainerEntity> findInactiveTrainersSince(@Param("cutoffDate") LocalDate cutoffDate);

}
