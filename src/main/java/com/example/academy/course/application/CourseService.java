package com.example.academy.course.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.common.presentation.dto.PagingRequest;
import com.example.academy.common.presentation.dto.PagingResponse;
import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseRepository;
import com.example.academy.course.presentation.dto.request.RegisterCourseRequest;
import com.example.academy.course.presentation.dto.response.CourseClassmateInfoResponse;
import com.example.academy.course.presentation.dto.response.CourseDetailResponse;
import com.example.academy.course.presentation.dto.response.CourseSummaryResponse;
import com.example.academy.enrollment.domain.EnrollmentRepository;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
	private final CourseRepository courseRepository;
	private final UserRepository userRepository;
	private final EnrollmentRepository enrollmentRepository;

	@Transactional
	public Long registerCourse(RegisterCourseRequest request, Long creatorId) {

		User creator = userRepository.findById(creatorId)
			.orElseThrow(() -> new NotFoundException(User.class));

		Course course = courseRepository.save(request.toEntity(creator));
		return course.getId();
	}

	public CourseDetailResponse getCourseDetail(Long courseId) {
		Course course = courseRepository.findByIdWithCreator(courseId)
			.orElseThrow(() -> new NotFoundException(Course.class));

		return CourseDetailResponse.from(course);
	}

	public PagingResponse<CourseSummaryResponse> getCourses(String state, PagingRequest request) {
		return PagingResponse.from(
			courseRepository.findPageByCourseStateIn(state, request.page(), request.size(), request.sort())
				.map(CourseSummaryResponse::of)
		);
	}

	public PagingResponse<CourseClassmateInfoResponse> getCourseClassmates(Long courseId, Long creatorId, PagingRequest request) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new NotFoundException(Course.class));

		if (!course.canAccess(creatorId)) {
			throw new ForbiddenException();
		}

		return PagingResponse.from(
			enrollmentRepository.findPageByCourseIdAndState(courseId, request.page(), request.size())
				.map(CourseClassmateInfoResponse::from)
		);
	}
}
