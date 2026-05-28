package com.example.academy.course.presentation.dto.request;

import java.time.LocalDate;

import com.example.academy.course.domain.Capacity;
import com.example.academy.course.domain.Course;
import com.example.academy.identity.domain.user.User;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterCourseRequest(
	@NotBlank String title,
	@NotBlank String description,
	@NotNull @Min(0) Integer price,
	@NotNull @Min(1) Integer maxCapacity,
	@NotNull LocalDate startDate,
	@NotNull LocalDate endDate
) {
	public Course toEntity(User creator) {
		return Course.of(title, description, price, new Capacity(maxCapacity), startDate, endDate, creator);
	}
}
