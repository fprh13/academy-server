package com.example.academy.identity.presentation;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.identity.application.UserService;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.presentation.dto.request.user.RegisterUserRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> register(@RequestBody @Valid RegisterUserRequest registerUserRequest) {
        return ResponseEntity.status(OK).body(ApiResponse.of(userService.register(registerUserRequest)));
    }

	@GetMapping("/login-id/exists")
	public ResponseEntity<ApiResponse<Object>> checkDuplicateLoginId(@RequestParam String loginId) {
		userService.checkDuplicateLoginId(loginId);
		return ResponseEntity.status(OK).body(ApiResponse.of());
	}

	@GetMapping("/email/exists")
	public ResponseEntity<ApiResponse<Object>> checkDuplicateEmail(@RequestParam String email) {
		userService.checkDuplicateEmail(email);
		return ResponseEntity.status(OK).body(ApiResponse.of());
	}

	@GetMapping("/profile")
	public ResponseEntity<ApiResponse<Object>> getProfileInfo(User user) {
		return ResponseEntity.status(OK).body(ApiResponse.of(userService.getProfileInfo(user)));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<Object>> getPublicProfileInfo(@PathVariable Long userId) {
		return ResponseEntity.status(OK).body(ApiResponse.of(userService.getPublicProfileInfo(userId)));
	}
}
