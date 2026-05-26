package com.example.academy.enrollment.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;

public interface EnrollmentRepository {
	Enrollment save(Enrollment enrollment);
	Optional<Enrollment> findById(Long enrollmentId);
	void deleteById(Long enrollmentId);
	Page<Enrollment> findPageByUserIdAndStateIn(Long userId, String state, int page, int size, String sort);
	Page<Enrollment> findPageByCourseIdAndState(Long courseId, int page, int size);
}
