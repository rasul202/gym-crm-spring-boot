package com.epam.gymcrmspringboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

/**
 * JPA entity representing the {@code training_type} table.
 * Defines types of training (e.g., Yoga, Cardio).
 */
@Entity
@Table(name = "training_type")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
@FieldDefaults(level = PRIVATE)
public class TrainingTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "training_type_name", nullable = false, unique = true, length = 100)
    String trainingTypeName;

}

