package com.epam.gymcrmspringboot.service;

import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTraineeProfileRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeProfileResponse;
import com.epam.gymcrmspringboot.dto.response.RegistrationResponse;
import com.epam.gymcrmspringboot.dto.response.TraineeResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTraineeProfileResponse;
import com.epam.gymcrmspringboot.model.TraineeEntity;

import org.springframework.security.core.Authentication;
import java.util.List;

public interface TraineeService {

    RegistrationResponse registerTrainee(CreateTraineeRequest request);

    GetTraineeProfileResponse getTraineeByUsername(String username, Authentication authentication);

    UpdateTraineeProfileResponse updateTrainee(String username, Authentication authentication, UpdateTraineeProfileRequest request);

    List<TrainerSummary> updateTraineeTrainers(String username, Authentication authentication, List<String> trainerUsernames);

    void deactivateTrainee(String username, Authentication authentication);

    void activateTrainee(String username, Authentication authentication);

    void deleteTrainee(String username, Authentication authentication);

    boolean isRegisteredAsTrainee(String firstName, String lastName);

    TraineeEntity getTraineeByUsername(String username);

    TraineeEntity getTraineeByUsernameWithTrainings(String username);

    List<TrainerSummary> getAvailableTrainersForTrainee(String traineeUsername, Authentication authentication);

}
