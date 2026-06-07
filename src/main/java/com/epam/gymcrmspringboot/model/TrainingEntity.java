package com.epam.gymcrmspringboot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

/**
 * JPA entity representing the {@code training} table.
 * Records training sessions linking trainees, trainers, and training types.
 */
@Entity
@Table(name = "training")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"trainee", "trainer", "trainingType"})
@EqualsAndHashCode(of = "id")
@FieldDefaults(level = PRIVATE)
public class TrainingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "trainee_id", nullable = false)
    TraineeEntity trainee;

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    TrainerEntity trainer;

    @Column(name = "training_name", nullable = false, length = 255)
    String trainingName;

    @ManyToOne
    @JoinColumn(name = "training_type_id", nullable = false)
    TrainingTypeEntity trainingType;

    @Column(name = "training_date", nullable = false)
    LocalDate trainingDate;

    @Column(name = "training_duration", nullable = false)
    Integer trainingDuration;

  }
