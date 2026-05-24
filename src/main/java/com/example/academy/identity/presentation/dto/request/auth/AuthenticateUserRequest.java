package com.example.academy.identity.presentation.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AuthenticateUserRequest(
	@NotBlank
	@Pattern(regexp = "^[A-Za-z0-9]{4,20}$")
	String loginId,

	@NotBlank
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*?_~])[A-Za-z\\d!@#$%^&*?_~]{8,16}$")
	String password
) {
}
