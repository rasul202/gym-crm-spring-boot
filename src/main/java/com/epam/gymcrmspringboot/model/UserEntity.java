package com.epam.gymcrmspringboot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

/**
 * JPA entity representing the {@code app_user} table.
 * Stores basic user authentication and profile information.
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
@FieldDefaults(level = PRIVATE)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    String lastName;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    String username;

    @Column(name = "password", nullable = false, length = 255)
    String password;

    @Column(name = "is_active", nullable = false)
    Boolean isActive;

    @PrePersist
    public void setDefaults(){
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

}

