package com.example.academy.enrollment.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.enrollment.application.EnrollmentService;
import com.example.academy.enrollment.presentation.dto.request.ApplyEnrollmentRequest;
import com.example.academy.identity.domain.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

	private final EnrollmentService enrollmentService;

	@PostMapping
	public ResponseEntity<ApiResponse<Long>> apply(@Valid @RequestBody ApplyEnrollmentRequest request, User user) {
		return  ResponseEntity.ok(ApiResponse.of(enrollmentService.apply(request.courseId(), user.getId())));
	}

	@PostMapping("/{enrollmentId}/confirm")
	public ResponseEntity<ApiResponse<Void>> confirm(@PathVariable Long enrollmentId, User user) {
		enrollmentService.confirm(enrollmentId, user.getId());
		return ResponseEntity.ok(ApiResponse.of());
	}
}
