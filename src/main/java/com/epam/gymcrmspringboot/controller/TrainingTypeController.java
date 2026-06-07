package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.response.TrainingTypeResponse;
import com.epam.gymcrmspringboot.service.TrainingTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/training-types")
@Api(tags = "Training Types")
public class TrainingTypeController {

    private TrainingTypeService trainingTypeService;

    @Autowired
    public void setTrainingTypeService(TrainingTypeService trainingTypeService) {this.trainingTypeService = trainingTypeService;}

    @GetMapping
    @ApiOperation(value = "Get all training types", notes = "Returns all supported training types")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training types returned successfully")
    })
    public ResponseEntity<List<TrainingTypeResponse>> getAllTrainingTypes() {
        return ResponseEntity.ok(trainingTypeService.getAllTrainingTypes());
    }
}

