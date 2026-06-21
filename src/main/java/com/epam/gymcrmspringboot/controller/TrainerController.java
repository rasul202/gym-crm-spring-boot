package com.epam.gymcrmspringboot.controller;
import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTrainerProfileRequest;
import com.epam.gymcrmspringboot.dto.response.*;
import com.epam.gymcrmspringboot.service.TrainerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trainers")
@Api(tags = "Trainers")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class TrainerController {

    TrainerService trainerService;

    @PostMapping
    @ApiOperation(
            value = "Register trainer",
            notes = "Creates a trainer profile and returns generated credentials. "
                    + "The system generates a random password. "
                    + "Username is based on firstName.lastName; if it already exists, "
                    + "sequential digits are appended to make it unique."
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Trainer registered successfully")
    })
    public ResponseEntity<RegistrationResponse> registerTrainer(@RequestBody @Valid CreateTrainerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerService.registerTrainer(request));
    }

    @GetMapping("/{username}")
    @ApiOperation(value = "Get trainer profile", notes = "Returns the trainer profile for the specified username.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile returned successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainer not found or deactivated")
    })
     public ResponseEntity<GetTrainerProfileResponse> getTrainerProfile(
             @PathVariable String username,
             Authentication authentication) {
         return ResponseEntity.ok(trainerService.getTrainerByUsername(username, authentication));
     }

    @PutMapping("/{username}")
    @ApiOperation(value = "Update trainer profile", notes = "Updates trainer profile fields for an authenticated trainer.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile updated successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainer not found or deactivated")
    })
     public ResponseEntity<UpdateTrainerProfileResponse> updateTrainerProfile(
             @PathVariable String username,
             Authentication authentication,
             @RequestBody @Valid UpdateTrainerProfileRequest body) {
         return ResponseEntity.ok(trainerService.updateTrainer(username, authentication, body));
     }

    @PatchMapping("/{username}/status")
    @ApiOperation(
            value = "Activate or deactivate trainer",
            notes = "Sets trainer status using the isActive request parameter."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer status updated successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainer not found or deactivated")
    })
    public ResponseEntity<Void> activateDeactivateTrainer(
            @PathVariable String username,
            Authentication authentication,
            @RequestParam("isActive")  @NotNull(message = "isActive must not be null") Boolean isActive) {
         if (Boolean.TRUE.equals(isActive)) {
             trainerService.activateTrainer(username, authentication);
         } else {
             trainerService.deactivateTrainer(username, authentication);
         }
         return ResponseEntity.ok().build();
     }

}

