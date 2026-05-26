package com.example.academy.course.presentation.dto.response;

import com.example.academy.enrollment.domain.Enrollment;

public record CourseClassmateInfoResponse(
	Long userId,
	String name,
	String email
) {
	public static CourseClassmateInfoResponse from(Enrollment enrollment) {
		return new CourseClassmateInfoResponse(
			enrollment.getUser().getId(),
			enrollment.getUser().getName(),
			enrollment.getUser().getEmail()
		);
	}
}
