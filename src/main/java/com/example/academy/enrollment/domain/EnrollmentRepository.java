package com.example.academy.enrollment.domain;

import java.util.Optional;


public interface EnrollmentRepository {
	Enrollment save(Enrollment enrollment);
	Optional<Enrollment> findById(Long enrollmentId);
	void deleteById(Long enrollmentId);
}
