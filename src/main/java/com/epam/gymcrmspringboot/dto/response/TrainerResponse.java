package com.epam.gymcrmspringboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerResponse {

    private String specialization;
    private Boolean isActive;
    private UserResponse user;
    private List<TraineeSummary> trainees;
}
