package com.example.academy.course.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.common.presentation.dto.PagingRequest;
import com.example.academy.common.presentation.dto.PagingResponse;
import com.example.academy.course.application.CourseService;
import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseRepository;
import com.example.academy.course.domain.CourseState;
import com.example.academy.course.presentation.dto.request.RegisterCourseRequest;
import com.example.academy.course.presentation.dto.response.CourseClassmateInfoResponse;
import com.example.academy.course.presentation.dto.response.CourseDetailResponse;
import com.example.academy.course.presentation.dto.response.CourseSummaryResponse;
import com.example.academy.enrollment.domain.Enrollment;
import com.example.academy.enrollment.domain.EnrollmentRepository;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.support.IntegrationSupportTest;
import com.example.academy.support.fixture.CourseFixture;
import com.example.academy.support.fixture.UserFixture;

class CourseIntegrationTest extends IntegrationSupportTest {

	private static final String COURSE_NOT_SAVED_MESSAGE = "강의가 저장되지 않았습니다.";

	@Autowired
	private CourseService courseService;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EnrollmentRepository enrollmentRepository;

	@Nested
	@DisplayName("강의 생성 기능")
	class RegisterCourseTest {
		@Test
		void 강의를_생성한다() {
			//given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			RegisterCourseRequest request = createRequest();

			//when
			Long courseId = courseService.registerCourse(request, creator.getId());

			//then
			Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new AssertionError(COURSE_NOT_SAVED_MESSAGE));

			assertAll(
				() -> assertThat(course.getTitle()).isEqualTo(request.title()),
				() -> assertThat(course.getDescription()).isEqualTo(request.description()),
				() -> assertThat(course.getPrice()).isEqualTo(request.price()),
				() -> assertThat(course.getCapacity().getMax()).isEqualTo(request.maxCapacity()),
				() -> assertThat(course.getCapacity().getCurrent()).isZero(),
				() -> assertThat(course.getStartDate()).isEqualTo(request.startDate()),
				() -> assertThat(course.getEndDate()).isEqualTo(request.endDate()),
				() -> assertThat(course.getState()).isEqualTo(CourseState.DRAFT),
				() -> assertThat(course.getCreator().getId()).isEqualTo(creator.getId())
			);
		}

		@Test
		void 사용자가_없다면_예외를_반환한다() {
			//given
			RegisterCourseRequest request = createRequest();
			Long creatorId = 999L;

			//when & then
			assertThatThrownBy(() -> courseService.registerCourse(request, creatorId))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 강사가_아니라면_예외를_반환한다() {
			//given
			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			RegisterCourseRequest request = createRequest();

			//when & then
			assertThatThrownBy(() -> courseService.registerCourse(request, user.getId()))
				.isInstanceOf(ForbiddenException.class);
		}
	}

	@Nested
	@DisplayName("강의 상세 조회 기능")
	class GetCourseDetailTest {
		@Test
		void 강의_상세정보를_조회한다() {
			//given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course savedCourse = createSavedCourse(creator);

			//when
			CourseDetailResponse response = courseService.getCourseDetail(savedCourse.getId());

			//then
			assertAll(
				() -> assertThat(response.courseId()).isEqualTo(savedCourse.getId()),
				() -> assertThat(response.title()).isEqualTo(savedCourse.getTitle()),
				() -> assertThat(response.description()).isEqualTo(savedCourse.getDescription()),
				() -> assertThat(response.price()).isEqualTo(savedCourse.getPrice()),
				() -> assertThat(response.maxCapacity()).isEqualTo(savedCourse.getCapacity().getMax()),
				() -> assertThat(response.enrollmentCount()).isEqualTo(savedCourse.getCapacity().getCurrent()),
				() -> assertThat(response.startDate()).isEqualTo(savedCourse.getStartDate()),
				() -> assertThat(response.endDate()).isEqualTo(savedCourse.getEndDate()),
				() -> assertThat(response.creatorInfo().creatorId()).isEqualTo(creator.getId()),
				() -> assertThat(response.creatorInfo().creatorName()).isEqualTo(creator.getName()),
				() -> assertThat(response.creatorInfo().creatorEmail()).isEqualTo(creator.getEmail())
			);
		}

		@Test
		void 강의가_없다면_예외를_반환한다() {
			//given
			Long courseId = 999L;

			//when & then
			assertThatThrownBy(() -> courseService.getCourseDetail(courseId))
				.isInstanceOf(NotFoundException.class);
		}
	}

	@Nested
	@DisplayName("강의 목록 페이징 조회 기능")
	class GetCoursesTest {
		@Test
		void 모집중인_강의만_페이지로_조회한다() {
			//given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course openCourse = createSavedOpenCourse(creator, CourseFixture.COURSE_FIXTURE_1);
			createSavedClosedCourse(creator, CourseFixture.COURSE_FIXTURE_2);
			createSavedDraftCourse(creator, CourseFixture.COURSE_FIXTURE_3);

			PagingRequest request = new PagingRequest(1, 10, "deadline");

			//when
			PagingResponse<CourseSummaryResponse> response = courseService.getCourses("open", request);

			//then
			assertAll(
				() -> assertThat(response.content()).hasSize(1),
				() -> assertThat(response.content().get(0).courseId()).isEqualTo(openCourse.getId()),
				() -> assertThat(response.content().get(0).creatorName()).isEqualTo(creator.getName()),
				() -> assertThat(response.page().number()).isEqualTo(1),
				() -> assertThat(response.page().size()).isEqualTo(10),
				() -> assertThat(response.page().totalElements()).isEqualTo(1),
				() -> assertThat(response.page().totalPages()).isEqualTo(1),
				() -> assertThat(response.page().hasNext()).isFalse(),
				() -> assertThat(response.page().hasPrevious()).isFalse()
			);
		}

		@Test
		void 기본조회는_모집중과_마감강의를_정렬과_함께_페이지로_조회한다() {
			//given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			createSavedOpenCourse(creator, CourseFixture.COURSE_FIXTURE_1);
			createSavedOpenCourse(creator, CourseFixture.COURSE_FIXTURE_2);
			Course thirdCourse = createSavedClosedCourse(creator, CourseFixture.COURSE_FIXTURE_3);
			createSavedDraftCourse(creator, CourseFixture.COURSE_FIXTURE_1);

			PagingRequest request = new PagingRequest(2, 2, "deadline");

			//when
			PagingResponse<CourseSummaryResponse> response = courseService.getCourses(null, request);

			//then
			assertAll(
				() -> assertThat(response.content()).hasSize(1),
				() -> assertThat(response.content().get(0).courseId()).isEqualTo(thirdCourse.getId()),
				() -> assertThat(response.page().number()).isEqualTo(2),
				() -> assertThat(response.page().size()).isEqualTo(2),
				() -> assertThat(response.page().totalElements()).isEqualTo(3),
				() -> assertThat(response.page().totalPages()).isEqualTo(2),
				() -> assertThat(response.page().hasNext()).isFalse(),
				() -> assertThat(response.page().hasPrevious()).isTrue()
			);
		}
	}

	@Nested
	@DisplayName("강의 수강생 목록 페이징 조회 기능")
	class GetCourseClassmatesTest {
		@Test
		void 본인_강의의_결제확정_수강생만_페이지로_조회한다() {
			//given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator, CourseFixture.COURSE_FIXTURE_1);

			User confirmedUser = createSavedUser(UserFixture.USER_FIXTURE_2.create());
			createSavedConfirmedEnrollment(course, confirmedUser);

			User paddingUser = createSavedUser(UserFixture.USER_FIXTURE_3.create());
			createSavedPendingEnrollment(course, paddingUser);

			PagingRequest request = new PagingRequest(1, 10, null);

			//when
			PagingResponse<CourseClassmateInfoResponse> response = courseService.getCourseClassmates(
				course.getId(),
				creator.getId(),
				request
			);

			//then
			assertAll(
				() -> assertThat(response.content()).hasSize(1),
				() -> assertThat(response.content().get(0).userId()).isEqualTo(confirmedUser.getId()),
				() -> assertThat(response.content().get(0).name()).isEqualTo(confirmedUser.getName()),
				() -> assertThat(response.content().get(0).email()).isEqualTo(confirmedUser.getEmail()),
				() -> assertThat(response.page().number()).isEqualTo(1),
				() -> assertThat(response.page().size()).isEqualTo(10),
				() -> assertThat(response.page().totalElements()).isEqualTo(1),
				() -> assertThat(response.page().totalPages()).isEqualTo(1),
				() -> assertThat(response.page().hasNext()).isFalse(),
				() -> assertThat(response.page().hasPrevious()).isFalse()
			);
		}

		@Test
		void 강의가_없다면_예외를_반환한다() {
			//given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			PagingRequest request = new PagingRequest(1, 10, null);

			//when & then
			assertThatThrownBy(() -> courseService.getCourseClassmates(999L, creator.getId(), request))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 본인_강의가_아니면_예외를_반환한다() {
			//given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			User otherCreator = userRepository.save(User.registerAsCreator(
				"creator2",
				"test2@1234",
				"creator2@test.com",
				"다른강사"
			));
			Course course = createSavedOpenCourse(creator, CourseFixture.COURSE_FIXTURE_1);
			PagingRequest request = new PagingRequest(1, 10, null);

			//when & then
			assertThatThrownBy(() -> courseService.getCourseClassmates(course.getId(), otherCreator.getId(), request))
				.isInstanceOf(ForbiddenException.class);
		}
	}

	private Course createSavedCourse(User creator) {
		Course course = courseRepository.save(CourseFixture.COURSE_FIXTURE_1.create(creator));
		course.open();
		course.increaseEnrollmentCount();
		return course;
	}

	private Course createSavedOpenCourse(User creator, CourseFixture fixture) {
		Course course = courseRepository.save(fixture.create(creator));
		course.open();
		return course;
	}

	private Course createSavedClosedCourse(User creator, CourseFixture fixture) {
		Course course = createSavedOpenCourse(creator, fixture);
		course.close();
		return course;
	}

	private Course createSavedDraftCourse(User creator, CourseFixture fixture) {
		return courseRepository.save(fixture.create(creator));
	}

	private Enrollment createSavedPendingEnrollment(Course course, User user) {
		return enrollmentRepository.save(Enrollment.apply(course, user));
	}

	private Enrollment createSavedConfirmedEnrollment(Course course, User user) {
		Enrollment enrollment = createSavedPendingEnrollment(course, user);
		enrollment.confirmPayment(java.time.LocalDateTime.of(2026, 6, 1, 10, 0));
		return enrollment;
	}

	private User createSavedUser(User user) {
		return userRepository.save(User.register(
			user.getLoginId(),
			user.getPassword(),
			user.getEmail(),
			user.getName()
		));
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
