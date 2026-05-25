package com.example.academy.course.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.academy.course.domain.Course;

public interface JpaCourseRepository extends JpaRepository<Course, Long> {

	@Query("SELECT c FROM Course c LEFT JOIN FETCH c.user u WHERE c.id = :id")
	Optional<Course> findByIdWithCreator(@Param("id") Long id);
}
