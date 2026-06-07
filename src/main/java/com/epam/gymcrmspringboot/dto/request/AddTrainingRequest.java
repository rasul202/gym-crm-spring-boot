package com.epam.gymcrmspringboot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTrainingRequest {

    @NotBlank(message = "traineeUsername must not be blank")
    private String traineeUsername;

    @NotBlank(message = "trainerUsername must not be blank")
    private String trainerUsername;

    @NotBlank(message = "trainingName must not be blank")
    private String trainingName;

    @NotNull(message = "trainingDate must not be null")
    private LocalDate trainingDate;

    @NotNull(message = "trainingDuration must not be null")
    @Positive(message = "trainingDuration must be positive")
    private Integer trainingDuration;
}

