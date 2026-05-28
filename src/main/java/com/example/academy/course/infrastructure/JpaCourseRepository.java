package com.example.academy.course.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseState;

import jakarta.persistence.LockModeType;

public interface JpaCourseRepository extends JpaRepository<Course, Long> {

	@Query("SELECT c FROM Course c LEFT JOIN FETCH c.creator cr WHERE c.id = :id")
	Optional<Course> findByIdWithCreator(@Param("id") Long id);

	@Query("SELECT c FROM Course c LEFT JOIN FETCH c.creator WHERE c.state IN :states")
	Page<Course> findPageByCourseStateIn(@Param("states") List<CourseState> states, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Course c where c.id = :courseId")
	Optional<Course> findByIdForUpdate(Long courseId);
}
