package com.epam.gymcrmspringboot.controller;


import com.epam.gymcrmspringboot.dto.request.ChangePasswordRequest;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Api(tags = "Authentication")
public class AuthController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {this.userService = userService;}

    @GetMapping("/login")
    @ApiOperation(value = "Authenticate user", notes = "Validates user credentials and checks user is active")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Authentication successful"),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        boolean authenticated = userService.authenticateActiveUser(
                LoginRequest.builder().username(username).password(password).build() );
        return authenticated ? ResponseEntity.ok().build() : ResponseEntity.status(401).build();
    }

    @PutMapping("/password")
    @ApiOperation(value = "Change user password", notes = "Changes password for an authenticated active user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password changed successfully"),
            @ApiResponse(code = 400, message = "Validation failed for request body"),
            @ApiResponse(code = 401, message = "Invalid current credentials")
    })
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        boolean authenticated = userService.authenticateActiveUser(
                LoginRequest.builder()
                        .username(request.getUsername())
                        .password(request.getOldPassword())
                        .build());
        if (!authenticated) {
            return ResponseEntity.status(401).build();
        }
        userService.changePassword(request.getUsername(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}

