package com.epam.gymcrmspringboot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTraineeTrainersListRequest {

    @NotEmpty(message = "trainers list must not be empty")
    private List<@NotBlank(message = "trainer username must not be blank") String> trainerUsernames;
}

