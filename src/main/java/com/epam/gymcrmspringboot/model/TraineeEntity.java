package com.epam.gymcrmspringboot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

/**
 * JPA entity representing the {@code trainee} table.
 * Linked to a user account via {@code user_id} foreign key.
 */
@Entity
@Table(name = "trainee")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"trainings", "user"})
@EqualsAndHashCode(of = "id")
@FieldDefaults(level = PRIVATE)
public class TraineeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "date_of_birth", nullable = true)
    LocalDate dateOfBirth;

    @Column(name = "address", length = 255, nullable = true)
    String address;

    @OneToOne(cascade = {CascadeType.REMOVE,CascadeType.MERGE})
    @JoinColumn(name = "user_id" )
    UserEntity user;

    @OneToMany(mappedBy = "trainee", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE,CascadeType.MERGE} , orphanRemoval = true )
    List<TrainingEntity> trainings;

}

