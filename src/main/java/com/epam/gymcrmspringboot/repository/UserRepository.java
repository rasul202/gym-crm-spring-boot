package com.epam.gymcrmspringboot.repository;

import com.epam.gymcrmspringboot.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByUsernameAndIsActiveTrue(String username);
    @Query("""
            SELECT u
            FROM UserEntity u
           LEFT JOIN FETCH u.trainer tr
           LEFT JOIN FETCH u.trainee te
            WHERE u.username = :username
              AND u.isActive = true
            """)
    Optional<UserEntity> findActiveUserWithRoleLinks(@Param("username") String username);
    boolean existsByUsername(String username);

}
