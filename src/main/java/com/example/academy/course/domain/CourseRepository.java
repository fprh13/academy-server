package com.example.academy.course.domain;

import java.util.Optional;

public interface CourseRepository {
	Course save(Course course);
	Optional<Course> findById(Long courseId);
	void deleteById(Long courseId);
}
