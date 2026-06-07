package com.epam.gymcrmspringboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingResponse {

    private String trainingName;
    private String trainingType;
    private LocalDate trainingDate;
    private Integer trainingDuration;
    private String trainerUsername;
    private String traineeUsername;

}

