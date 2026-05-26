package com.example.academy.enrollment.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.academy.enrollment.domain.Enrollment;
import com.example.academy.enrollment.domain.EnrollmentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EnrollmentRepositoryImpl implements EnrollmentRepository {

	private final JpaEnrollmentRepository jpaEnrollmentRepository;

	@Override
	public Enrollment save(Enrollment enrollment) {
		return jpaEnrollmentRepository.save(enrollment);
	}

	@Override
	public Optional<Enrollment> findById(Long enrollmentId) {
		return jpaEnrollmentRepository.findById(enrollmentId);
	}

	@Override
	public void deleteById(Long enrollmentId) {
		jpaEnrollmentRepository.deleteById(enrollmentId);
	}
}
