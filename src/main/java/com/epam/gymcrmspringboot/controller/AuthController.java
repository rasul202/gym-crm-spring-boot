package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.dto.response.LoginResponse;
import com.epam.gymcrmspringboot.service.AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Api(tags = "authentication")
@RequestMapping("/authentication")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    @ApiOperation(value = "Authenticate user", notes = "Validates user credentials and returns JWT token")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Authentication successful"),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        String token = authenticationService.authenticate(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }

}
