package com.epam.gymcrmspringboot.controller;


import com.epam.gymcrmspringboot.dto.request.ChangePasswordRequest;
import com.epam.gymcrmspringboot.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Api(tags = "Users")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PutMapping("/password")
    @ApiOperation(value = "Change user password", notes = "Changes password for an authenticated active user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password changed successfully"),
            @ApiResponse(code = 400, message = "Validation failed for request body"),
            @ApiResponse(code = 401, message = "Invalid current credentials")
    })
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request, Authentication authentication) {
        userService.changePassword(request, authentication);
        return ResponseEntity.ok().build();
    }
}

