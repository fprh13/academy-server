package com.example.academy.identity.presentation;

import static com.example.academy.identity.infrastructure.jwt.JwtConstants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.identity.application.AuthService;
import com.example.academy.identity.presentation.dto.request.auth.AuthenticateUserRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> authenticate(@RequestBody @Valid AuthenticateUserRequest authenticateUserRequest) {
        String accessToken = authService.authenticate(authenticateUserRequest).accessToken();
        return ResponseEntity.ok().body(ApiResponse.of(BEARER_PREFIX + accessToken));
    }
}
