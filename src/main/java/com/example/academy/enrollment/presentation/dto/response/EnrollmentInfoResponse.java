package com.example.academy.enrollment.presentation.dto.response;

import java.time.LocalDateTime;

import com.example.academy.course.domain.Course;
import com.example.academy.enrollment.domain.Enrollment;

public record EnrollmentInfoResponse(
	Long enrollmentId,
	String state,
	LocalDateTime createAt,
	LocalDateTime paidAt,
	CourseInfo courseInfo
) {
	public static EnrollmentInfoResponse from(Enrollment enrollment) {
		return new EnrollmentInfoResponse(
			enrollment.getId(),
			enrollment.getState().name(),
			enrollment.getCreateAt(),
			enrollment.getPaidAt(),
			CourseInfo.from(enrollment.getCourse())
		);
	}

	public record CourseInfo(
		Long courseId,
		String courseName,
		int coursePrice
	) {
		public static CourseInfo from(Course course) {
			return new CourseInfo(
				course.getId(),
				course.getCreator().getName(),
				course.getPrice()
			);
		}
	}
}
