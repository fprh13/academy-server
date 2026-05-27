package com.example.academy.enrollment.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.common.presentation.dto.PagingRequest;
import com.example.academy.common.presentation.dto.PagingResponse;
import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseRepository;
import com.example.academy.enrollment.domain.Enrollment;
import com.example.academy.enrollment.domain.EnrollmentRepository;
import com.example.academy.enrollment.presentation.dto.response.EnrollmentInfoResponse;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

	private final EnrollmentRepository enrollmentRepository;
	private final CourseRepository courseRepository;
	private final UserRepository userRepository;

	@Transactional
	public Long apply(Long CourseId, Long userId) {
		Course course = courseRepository.findById(CourseId)
			.orElseThrow(() -> new NotFoundException(Course.class));
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(User.class));

		return enrollmentRepository.save(Enrollment.apply(course, user)).getId();
	}

	@Transactional
	public void confirm(Long enrollmentId,  Long userId) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
			.orElseThrow(() -> new NotFoundException(Enrollment.class));

		if (!enrollment.canAccess(userId)) {
			throw new ForbiddenException();
		}

		LocalDateTime now = LocalDateTime.now();
		enrollment.confirmPayment(now);
	}

	@Transactional
	public void cancel(Long enrollmentId, Long userId) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
			.orElseThrow(() -> new NotFoundException(Enrollment.class));

		if (!enrollment.canAccess(userId)) {
			throw new ForbiddenException();
		}

		enrollment.cancelApplication();

		enrollmentRepository.deleteById(enrollmentId);
	}


	@Transactional
	public void cancelConfirm(Long enrollmentId, Long userId) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
			.orElseThrow(() -> new NotFoundException(Enrollment.class));

		if (!enrollment.canAccess(userId)) {
			throw new ForbiddenException();
		}

		LocalDateTime now = LocalDateTime.now();
		enrollment.cancelConfirmed(now);
	}

	public PagingResponse<EnrollmentInfoResponse> gets(PagingRequest request, String state, Long userId) {
		return PagingResponse.from(
			enrollmentRepository.findPageByUserIdAndStateIn(userId, state, request.page(), request.size(), request.sort())
				.map(EnrollmentInfoResponse::from)
		);
	}
}
