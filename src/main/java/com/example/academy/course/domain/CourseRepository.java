package com.example.academy.course.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

public interface CourseRepository {
	Course save(Course course);
	Optional<Course> findById(Long courseId);
	void deleteById(Long courseId);
	Optional<Course> findByIdWithCreator(Long id);
	Page<Course> findPageByCourseStateIn(String state, int page, int size, String sort);
}
