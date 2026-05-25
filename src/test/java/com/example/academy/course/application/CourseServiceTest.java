package com.example.academy.course.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseRepository;
import com.example.academy.course.presentation.dto.request.RegisterCourseRequest;
import com.example.academy.course.presentation.dto.response.CourseDetailResponse;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

	@InjectMocks
	private CourseService courseService;

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private UserRepository userRepository;

	@Nested
	@DisplayName("강의 생성 기능")
	class RegisterCourseTest {
		@Test
		void 사용자를_조회한다() {
			//given
			User creator = createCreator();
			RegisterCourseRequest request = createRequest();

			Mockito.when(userRepository.findById(creator.getId()))
				.thenReturn(Optional.of(creator));
			Mockito.when(courseRepository.save(any(Course.class)))
				.thenReturn(request.toEntity(creator));

			//when
			courseService.registerCourse(request, creator.getId());

			//then
			Mockito.verify(userRepository, Mockito.times(1))
				.findById(creator.getId());
		}

		@Test
		void 사용자가_없다면_예외를_반환한다() {
			//given
			RegisterCourseRequest request = createRequest();
			Long creatorId = 1L;

			Mockito.when(userRepository.findById(creatorId))
				.thenReturn(Optional.empty());

			//when & then
			assertThatThrownBy(() -> courseService.registerCourse(request, creatorId))
				.isInstanceOf(NotFoundException.class);
			Mockito.verify(courseRepository, Mockito.never()).save(any(Course.class));
		}

		@Test
		void 강의를_생성한다() {
			//given
			User creator = createCreator();
			RegisterCourseRequest request = createRequest();

			Mockito.when(userRepository.findById(creator.getId()))
				.thenReturn(Optional.of(creator));
			Mockito.when(courseRepository.save(any(Course.class)))
				.thenReturn(request.toEntity(creator));

			//when
			courseService.registerCourse(request, creator.getId());

			//then
			Mockito.verify(courseRepository, Mockito.times(1))
				.save(any(Course.class));
		}

		@Test
		void 강사가_아니라면_예외를_반환한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			ReflectionTestUtils.setField(user, "id", 1L);

			RegisterCourseRequest request = createRequest();

			Mockito.when(userRepository.findById(user.getId()))
				.thenReturn(Optional.of(user));

			//when & then
			assertThatThrownBy(() -> courseService.registerCourse(request, user.getId()))
				.isInstanceOf(ForbiddenException.class);
			Mockito.verify(courseRepository, Mockito.never()).save(any(Course.class));
		}

		@Test
		void 생성된_강의_ID를_반환한다() {
			//given
			User creator = createCreator();
			RegisterCourseRequest request = createRequest();
			Course course = request.toEntity(creator);

			Mockito.when(userRepository.findById(creator.getId()))
				.thenReturn(Optional.of(creator));

			ReflectionTestUtils.setField(course, "id", 10L);
			Mockito.when(courseRepository.save(any(Course.class)))
				.thenReturn(course);

			//when
			Long courseId = courseService.registerCourse(request, creator.getId());

			//then
			assertThat(courseId).isEqualTo(10L);
		}
	}

	@Nested
	@DisplayName("강의 상세 조회 기능")
	class GetCourseDetailTest {
		@Test
		void 강의와_강사정보를_함께_조회한다() {
			//given
			User creator = createCreator();
			Course course = createCourse(creator);
			Long courseId = course.getId();

			Mockito.when(courseRepository.findByIdWithCreator(courseId))
				.thenReturn(Optional.of(course));

			//when
			courseService.getCourseDetail(courseId);

			//then
			Mockito.verify(courseRepository, Mockito.times(1))
				.findByIdWithCreator(courseId);
		}

		@Test
		void 강의_상세응답을_반환한다() {
			//given
			User creator = createCreator();
			Course course = createCourse(creator);
			Long courseId = course.getId();

			Mockito.when(courseRepository.findByIdWithCreator(courseId))
				.thenReturn(Optional.of(course));

			//when
			CourseDetailResponse response = courseService.getCourseDetail(courseId);

			//then
			Assertions.assertAll(
				() -> assertThat(response.courseId()).isEqualTo(course.getId()),
				() -> assertThat(response.title()).isEqualTo(course.getTitle()),
				() -> assertThat(response.description()).isEqualTo(course.getDescription()),
				() -> assertThat(response.price()).isEqualTo(course.getPrice()),
				() -> assertThat(response.maxCapacity()).isEqualTo(course.getCapacity().getMax()),
				() -> assertThat(response.enrollmentCount()).isEqualTo(course.getCapacity().getCurrent()),
				() -> assertThat(response.startDate()).isEqualTo(course.getStartDate()),
				() -> assertThat(response.endDate()).isEqualTo(course.getEndDate()),
				() -> assertThat(response.creatorInfo().creatorId()).isEqualTo(creator.getId()),
				() -> assertThat(response.creatorInfo().creatorName()).isEqualTo(creator.getName()),
				() -> assertThat(response.creatorInfo().creatorEmail()).isEqualTo(creator.getEmail())
			);
		}

		@Test
		void 강의가_없다면_예외를_반환한다() {
			//given
			Long courseId = 999L;

			Mockito.when(courseRepository.findByIdWithCreator(courseId))
				.thenReturn(Optional.empty());

			//when & then
			assertThatThrownBy(() -> courseService.getCourseDetail(courseId))
				.isInstanceOf(NotFoundException.class);
		}
	}

	private User createCreator() {
		User creator = UserFixture.USER_FIXTURE_1.createCreator();
		ReflectionTestUtils.setField(creator, "id", 1L);
		return creator;
	}

	private Course createCourse(User creator) {
		RegisterCourseRequest request = createRequest();
		Course course = request.toEntity(creator);
		ReflectionTestUtils.setField(course, "id", 10L);
		course.open();
		course.increaseEnrollmentCount();
		return course;
	}

	private RegisterCourseRequest createRequest() {
		return new RegisterCourseRequest(
			"자바 입문",
			"자바 기초 문법을 학습하는 강의입니다.",
			100000,
			30,
			LocalDate.of(2026, 6, 1),
			LocalDate.of(2026, 6, 30)
		);
	}
}
