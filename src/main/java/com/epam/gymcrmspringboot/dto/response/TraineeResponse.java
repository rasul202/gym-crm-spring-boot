package com.epam.gymcrmspringboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraineeResponse {

    private LocalDate dateOfBirth;
    private String address;
    private Boolean isActive;
    private UserResponse user;
    private List<TrainerSummary> trainers;

}
