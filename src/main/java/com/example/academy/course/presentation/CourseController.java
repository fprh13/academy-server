package com.example.academy.course.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.course.application.CourseService;
import com.example.academy.course.presentation.dto.request.RegisterCourseRequest;
import com.example.academy.course.presentation.dto.response.CourseDetailResponse;
import com.example.academy.identity.domain.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

	private final CourseService courseService;

	@PostMapping
	public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody RegisterCourseRequest request, User creator) {
		return ResponseEntity.ok().body(ApiResponse.of(courseService.registerCourse(request, creator.getId())));
	}

	@GetMapping("/{courseId}")
	public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourse(@PathVariable Long courseId) {
		return ResponseEntity.ok().body(ApiResponse.of(courseService.getCourseDetail(courseId)));
	}
}
