package com.example.academy.enrollment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record ApplyEnrollmentRequest(
	@NotNull Long courseId
) {
}
