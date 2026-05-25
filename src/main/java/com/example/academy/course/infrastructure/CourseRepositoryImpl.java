package com.example.academy.course.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepository {

	private final JpaCourseRepository jpaCourseRepository;

	@Override
	public Course save(Course course) {
		return jpaCourseRepository.save(course);
	}

	@Override
	public Optional<Course> findById(Long courseId) {
		return jpaCourseRepository.findById(courseId);
	}

	@Override
	public void deleteById(Long courseId) {
		jpaCourseRepository.deleteById(courseId);
	}

	@Override
	public Optional<Course> findByIdWithCreator(Long id) {
		return jpaCourseRepository.findByIdWithCreator(id);
	}
}
