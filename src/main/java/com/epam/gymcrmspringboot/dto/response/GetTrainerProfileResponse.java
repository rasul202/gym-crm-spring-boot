package com.epam.gymcrmspringboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTrainerProfileResponse {

    private String firstName;
    private String lastName;
    private String specialization;
    private Boolean isActive;
    private List<TraineeSummary> trainees;
}

