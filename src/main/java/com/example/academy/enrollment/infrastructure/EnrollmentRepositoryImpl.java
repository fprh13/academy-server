package com.example.academy.enrollment.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.example.academy.enrollment.domain.Enrollment;
import com.example.academy.enrollment.domain.EnrollmentRepository;
import com.example.academy.enrollment.domain.EnrollmentState;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EnrollmentRepositoryImpl implements EnrollmentRepository {
	private static final String STATE_CANCEL = "cancelled";
	private static final String STATE_CONFIRM = "confirmed";

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

	@Override
	public Page<Enrollment> findPageByUserIdAndStateIn(Long userId, String state, int page, int size, String sort) {
		List<EnrollmentState> states = resolveEnrollmentStates(state);
		return jpaEnrollmentRepository.findPageByUserIdAndStateIn(
			userId,
			states,
			PageRequest.of(page, size, Sort.by(sort))
		);
	}

	private List<EnrollmentState> resolveEnrollmentStates(String state) {
		if (STATE_CANCEL.equalsIgnoreCase(state)) {
			return List.of(EnrollmentState.CANCELLED);
		}

		if (STATE_CONFIRM.equalsIgnoreCase(state)) {
			return List.of(EnrollmentState.CONFIRMED);
		}

		return List.of(EnrollmentState.PENDING, EnrollmentState.CONFIRMED);
	}
}
