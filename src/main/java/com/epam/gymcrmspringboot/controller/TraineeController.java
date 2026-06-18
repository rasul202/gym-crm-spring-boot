package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTraineeProfileRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTraineeTrainersListRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeProfileResponse;
import com.epam.gymcrmspringboot.dto.response.RegistrationResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTraineeProfileResponse;
import com.epam.gymcrmspringboot.service.TraineeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trainees")
@RequiredArgsConstructor
@Api(tags = "Trainees")
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class TraineeController {

    TraineeService traineeService;

    @PostMapping
    @ApiOperation(value = "Register trainee", notes = "Creates a new trainee profile and returns generated credentials")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Trainee registered successfully")
    })
    public ResponseEntity<RegistrationResponse> registerTrainee(@RequestBody @Valid CreateTraineeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(traineeService.registerTrainee(request));
    }

    @GetMapping("/{username}")
    @ApiOperation(value = "Get trainee profile", notes = "Returns trainee profile by username")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile returned successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found or deactivated")
    })
    public ResponseEntity<GetTraineeProfileResponse> getTraineeProfile(
            @PathVariable String username,
            @RequestHeader("Password") String password) {
        return ResponseEntity.ok(traineeService.getTraineeByUsername(username, password));
    }

    @DeleteMapping("/{username}")
    @ApiOperation(value = "Delete trainee profile", notes = "Removes trainee profile by username")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee deleted successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found or deactivated")
    })
    public ResponseEntity<Void> deleteTrainee(
            @PathVariable String username,
            @RequestHeader("Password") String password) {
        traineeService.deleteTrainee(username, password);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}")
    @ApiOperation(value = "Update trainee profile", notes = "Updates trainee profile fields for an authenticated user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile updated successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found or deactivated")
    })
    public ResponseEntity<UpdateTraineeProfileResponse> updateTraineeProfile(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @RequestBody @Valid UpdateTraineeProfileRequest request) {
        return ResponseEntity.ok(traineeService.updateTrainee(username, password, request));
    }

    @PatchMapping("/{username}/status")
    @ApiOperation(value = "Activate or deactivate trainee", notes = "Sets trainee status based on isActive parameter, deactivated trainee can not be deactivated and activated trainee can not be activated")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee status updated successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found or deactivated")
    })
    public ResponseEntity<Void> activateDeactivateTrainee(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @RequestParam("isActive") @NotNull(message = "isActive must not be null") Boolean isActive) {
        if (Boolean.TRUE.equals(isActive)) {
            traineeService.activateTrainee(username, password);
        } else {
            traineeService.deactivateTrainee(username, password);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/trainers")
    @ApiOperation(value = "Update trainee trainers", notes = "Delete all existing assignments between specified trainee and trainers then insert updated training assignments between them with default values.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee trainers updated successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found or deactivated")
    })
    public ResponseEntity<List<TrainerSummary>> updateTraineeTrainers(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @RequestBody @Valid UpdateTraineeTrainersListRequest request) {
        return ResponseEntity.ok(traineeService.updateTraineeTrainers(username, password, request.getTrainerUsernames()));
    }

    @GetMapping("/{traineeUsername}/available-trainers")
    @ApiOperation(value = "Get available trainers", notes = "Returns trainers not currently assigned to the specified trainee")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Available trainers returned successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found or deactivated")
    })
    public ResponseEntity<List<TrainerSummary>> getAvailableTrainersForTrainee(
            @PathVariable String traineeUsername,
            @RequestHeader("Password") String password) {
        return ResponseEntity.ok(traineeService.getAvailableTrainersForTrainee(traineeUsername , password));
    }
}
