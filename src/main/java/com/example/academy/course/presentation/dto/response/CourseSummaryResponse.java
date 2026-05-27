package com.example.academy.course.presentation.dto.response;

import java.time.LocalDate;

import com.example.academy.course.domain.Course;

public record CourseSummaryResponse(
	Long courseId,
	String creatorName,
	String title,
	Integer price,
	Integer maxCapacity,
	Integer enrollmentCount,
	String state,
	LocalDate startDate,
	LocalDate endDate
) {
	public static CourseSummaryResponse of(Course course) {
		return new CourseSummaryResponse(
			course.getId(),
			course.getCreator().getName(),
			course.getTitle(),
			course.getPrice(),
			course.getCapacity().getMax(),
			course.getCapacity().getCurrent(),
			course.getState().name(),
			course.getStartDate(),
			course.getEndDate()
		);
	}
}
