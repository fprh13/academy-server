package com.example.academy.course.presentation.dto.response;

import java.time.LocalDate;

import com.example.academy.course.domain.Course;
import com.example.academy.identity.domain.user.User;

public record CourseDetailResponse(
	Long courseId,
	String title,
	String description,
	Integer price,
	Integer maxCapacity,
	Integer enrollmentCount,
	LocalDate startDate,
	LocalDate endDate,
	CreatorInfo creatorInfo
) {
	public static CourseDetailResponse from(Course course) {
		return new CourseDetailResponse(
			course.getId(),
			course.getTitle(),
			course.getDescription(),
			course.getPrice(),
			course.getCapacity().getMax(),
			course.getCapacity().getCurrent(),
			course.getStartDate(),
			course.getEndDate(),
			CreatorInfo.from(course.getCreator())
		);
	}

	public record CreatorInfo(
		Long creatorId,
		String creatorName,
		String creatorEmail
	) {

		public static CreatorInfo from(User creator) {
			return new CreatorInfo(
				creator.getId(),
				creator.getName(),
				creator.getEmail()
			);
		}
	}
}
