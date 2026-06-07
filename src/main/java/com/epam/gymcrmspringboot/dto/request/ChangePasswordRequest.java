package com.epam.gymcrmspringboot.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "username must not be blank")
    private String username;

    @NotBlank(message = "oldPassword must not be blank")
    private String oldPassword;

    @NotBlank(message = "newPassword must not be blank")
    private String newPassword;
}

