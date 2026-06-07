package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.request.AddTrainingRequest;
import com.epam.gymcrmspringboot.dto.request.GetTraineeTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.request.GetTrainerTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeTrainingsResponse;
import com.epam.gymcrmspringboot.dto.response.GetTrainerTrainingsResponse;
import com.epam.gymcrmspringboot.service.TrainingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trainings")
@Api(tags = "Trainings")
public class TrainingController {

    private TrainingService trainingService;

    @Autowired
    public void setTrainingService(TrainingService trainingService) {this.trainingService = trainingService;}

    @PostMapping
    @ApiOperation(value = "Add training", notes = "Creates a new training for trainee-trainer pair if trainer successfully authenticated")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training added successfully"),
            @ApiResponse(code = 401, message = "Authentication failed for trainer")
    })
    public ResponseEntity<Void> addTraining(
            @RequestBody @Valid AddTrainingRequest request,
            @RequestHeader("Password") String trainerPassword) {
        trainingService.addTraining(request, trainerPassword);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainees/{username}")
    @ApiOperation(value = "Get trainee trainings", notes = "Returns trainee trainings that match provided criteria")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainings returned successfully"),
            @ApiResponse(code = 401, message = "Trainee authentication failed")
    })
    public ResponseEntity<List<GetTraineeTrainingsResponse>> getTraineeTrainings(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @RequestBody @Valid GetTraineeTrainingsCriteriaRequest criteria) {
        return ResponseEntity.ok(trainingService.getTraineeTrainings(username , password , criteria ));
    }

    @GetMapping("/trainers/{username}")
    @ApiOperation(value = "Get trainer trainings", notes = "Returns trainer trainings that match provided criteria")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainings returned successfully"),
            @ApiResponse(code = 401, message = "trainer authentication failed")
    })
    public ResponseEntity<List<GetTrainerTrainingsResponse>> getTrainerTrainings(
            @PathVariable() String username,
            @RequestHeader("Password") String password,
            @RequestBody @Valid GetTrainerTrainingsCriteriaRequest request) {
        return ResponseEntity.ok(trainingService.getTrainerTrainings(username , password , request));
    }
}
