package com.example.academy.course.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseRepository;
import com.example.academy.course.domain.CourseState;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepository {
	private static final String DEFAULT_SORT_FIELD = "startDate";
	private static final String DEADLINE_SORT_FIELD = "endDate";
	private static final String SORT_DEADLINE = "deadline";
	private static final String STATE_OPEN = "open";

	private final JpaCourseRepository jpaCourseRepository;

	@Override
	public Course save(Course course) {
		return jpaCourseRepository.save(course);
	}

	@Override
	public Optional<Course> findById(Long courseId) {
		return jpaCourseRepository.findById(courseId);
	}

	@Override
	public void deleteById(Long courseId) {
		jpaCourseRepository.deleteById(courseId);
	}

	@Override
	public Optional<Course> findByIdWithCreator(Long id) {
		return jpaCourseRepository.findByIdWithCreator(id);
	}

	@Override
	public Page<Course> findPageByCourseStateIn(String state, int page, int size, String sort) {
		Sort courseSort = resolveCourseSort(sort);
		List<CourseState> states = resolveCourseStates(state);

		return jpaCourseRepository.findPageByCourseStateIn(states, PageRequest.of(page, size, courseSort));
	}

	@Override
	public Optional<Course> findByIdForUpdate(Long courseId) {
		return jpaCourseRepository.findByIdForUpdate(courseId);
	}

	private Sort resolveCourseSort(String sort) {
		if (SORT_DEADLINE.equalsIgnoreCase(sort)) {
			return Sort.by(Sort.Direction.ASC, DEADLINE_SORT_FIELD);
		}
		return Sort.by(DEFAULT_SORT_FIELD);
	}

	private List<CourseState> resolveCourseStates(String state) {
		if (STATE_OPEN.equalsIgnoreCase(state)) {
			return List.of(CourseState.OPEN);
		}
		return List.of(CourseState.OPEN, CourseState.CLOSED);
	}


}
