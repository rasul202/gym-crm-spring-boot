package com.epam.gymcrmspringboot.repository;

import com.epam.gymcrmspringboot.model.TrainingEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TrainingCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<TrainingEntity> findTraineeTrainings(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainingType,
            String trainerName) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> query = criteriaBuilder.createQuery(TrainingEntity.class);
        Root<TrainingEntity> root = query.from(TrainingEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get("trainee").get("user").get("username"), traineeUsername));

        if (fromDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("trainingDate"), toDate));
        }
        if (trainerName != null && !trainerName.isBlank()) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("trainer").get("user").get("firstName")),
                    "%" + trainerName.trim().toLowerCase() + "%"));
        }
        if (trainingType != null && !trainingType.isBlank()) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("trainingType").get("trainingTypeName")),
                    "%" + trainingType.trim().toLowerCase() + "%"));
        }

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(query).getResultList();
    }

    public List<TrainingEntity> findTrainerTrainings(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> query = criteriaBuilder.createQuery(TrainingEntity.class);
        Root<TrainingEntity> root = query.from(TrainingEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get("trainer").get("user").get("username"), trainerUsername));

        if (fromDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("trainingDate"), toDate));
        }
        if (traineeName != null && !traineeName.isBlank()) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("trainee").get("user").get("firstName")),
                    "%" + traineeName.trim().toLowerCase() + "%"));
        }

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(query).getResultList();
    }
}

