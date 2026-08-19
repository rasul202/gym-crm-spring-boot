package com.epam.gymcrmspringboot.dto.request;

import com.epam.gymcrmspringboot.dto.ActionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadRequest {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;
    private LocalDate trainingDate;
    private Integer trainingDuration;
    private Long trainingId;
    private ActionType actionType;
}
