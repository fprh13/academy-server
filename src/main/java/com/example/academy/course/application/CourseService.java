package com.example.academy.course.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.academy.common.exception.NotFoundException;
import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseRepository;
import com.example.academy.course.presentation.dto.request.RegisterCourseRequest;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
	private final CourseRepository courseRepository;
	private final UserRepository userRepository;

	@Transactional
	public Long registerCourse(RegisterCourseRequest request, Long creatorId) {

		User creator = userRepository.findById(creatorId)
			.orElseThrow(() -> new NotFoundException(User.class));

		Course course = courseRepository.save(request.toEntity(creator));
		return course.getId();
	}
}
