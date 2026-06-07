package com.epam.gymcrmspringboot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

/**
 * JPA entity representing the {@code trainer} table.
 * The {@code specialization} field stores the foreign key to {@code training_type(id)}.
 * Linked to a user account via {@code user_id} foreign key.
 */
@Entity
@Table(name = "trainer")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"user", "trainings"})
@EqualsAndHashCode(of = "id")
@FieldDefaults(level = PRIVATE)
public class TrainerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "specialization")
    TrainingTypeEntity specialization;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    List<TrainingEntity> trainings;

}
