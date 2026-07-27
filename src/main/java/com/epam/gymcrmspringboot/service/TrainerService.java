package com.epam.gymcrmspringboot.service;

import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTrainerProfileRequest;
import com.epam.gymcrmspringboot.dto.response.GetTrainerProfileResponse;
import com.epam.gymcrmspringboot.dto.response.RegistrationResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTrainerProfileResponse;
import com.epam.gymcrmspringboot.model.TrainerEntity;

import org.springframework.security.core.Authentication;
import java.util.List;

public interface TrainerService {

    RegistrationResponse registerTrainer(CreateTrainerRequest request);

    UpdateTrainerProfileResponse updateTrainer(String username, Authentication authentication, UpdateTrainerProfileRequest request);

    GetTrainerProfileResponse getTrainerByUsername(String username, Authentication authentication);

    void deactivateTrainer(String username, Authentication authentication);

    void activateTrainer(String username, Authentication authentication);

    //Get trainers list that not assigned on trainee by trainee's username.
    List<TrainerSummary> getAvailableTrainersForTrainee(String traineeUsername, Authentication authentication);

    List<TrainerEntity> getAllTrainersUsernamesIn(List<String> usernames);

    boolean isRegisteredAsTrainer(String firstName, String lastName);

    //for internal use cases not for API calls
    TrainerEntity getTrainerByUsername(String username);

}
