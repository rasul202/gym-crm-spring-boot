package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.service.AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Api(tags = "authentication")
@RequestMapping("/authentication")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthenticationService authenticationService;
    @NonFinal
    @Value("${security.jwt.cookie-name:JWT_TOKEN}")
    String jwtCookieName;

    @PostMapping("/login")
    @ApiOperation(value = "Authenticate user", notes = "Validates user credentials and sets JWT in secure HttpOnly cookie")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Authentication successful"),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest request) {
        String token = authenticationService.authenticate(request.getUsername(), request.getPassword());
        ResponseCookie jwtCookie = ResponseCookie.from(jwtCookieName, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .build();
    }

    @PostMapping("/logout")
    @ApiOperation(value = "Logout user", notes = "Deletes JWT secure HttpOnly cookie")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Logout successful")
    })
    public ResponseEntity<Void> logout() {
        ResponseCookie clearJwtCookie = ResponseCookie.from(jwtCookieName, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearJwtCookie.toString())
                .build();
    }

}
