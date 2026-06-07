package com.epam.gymcrmspringboot.repository;

import com.epam.gymcrmspringboot.model.TraineeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TraineeRepository extends JpaRepository<TraineeEntity, Long> {

    Optional<TraineeEntity> findByUserUsernameAndUserIsActiveTrue(String username);

    @Query("SELECT t FROM TraineeEntity t LEFT JOIN FETCH t.trainings WHERE t.user.username = :username AND t.user.isActive = true")
    Optional<TraineeEntity> findByUserUsernameAndUserIsActiveTrueWithTrainings(@Param("username") String username);
}
