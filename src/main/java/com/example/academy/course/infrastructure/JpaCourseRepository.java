package com.example.academy.course.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.academy.course.domain.Course;

public interface JpaCourseRepository extends JpaRepository<Course, Long> {
}
