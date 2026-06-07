package com.epam.gymcrmspringboot.repository;

import com.epam.gymcrmspringboot.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByUsernameAndIsActiveTrue(String username);

}
