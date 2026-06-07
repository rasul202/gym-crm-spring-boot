package com.epam.gymcrmspringboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerSummary {

    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
}

