package com.example.academy.enrollment.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.academy.common.exception.ConflictException;
import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.common.exception.BadRequestException;
import com.example.academy.course.domain.Capacity;
import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseRepository;
import com.example.academy.enrollment.application.EnrollmentService;
import com.example.academy.enrollment.domain.Enrollment;
import com.example.academy.enrollment.domain.EnrollmentRepository;
import com.example.academy.enrollment.domain.EnrollmentState;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.support.IntegrationSupportTest;
import com.example.academy.support.fixture.CourseFixture;
import com.example.academy.support.fixture.UserFixture;

class EnrollmentIntegrationTest extends IntegrationSupportTest {

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private EnrollmentRepository enrollmentRepository;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private UserRepository userRepository;

	@Nested
	@DisplayName("수강 신청 기능")
	class ApplyEnrollmentTest {
		@Test
		void 수강_신청을_저장한다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			Course course = createSavedOpenCourse(creator);

			// when
			Long enrollmentId = enrollmentService.apply(course.getId(), user.getId());

			// then
			Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new AssertionError("수강 신청이 저장되지 않았습니다."));
			Course savedCourse = courseRepository.findById(course.getId())
				.orElseThrow(() -> new AssertionError("강의가 저장되지 않았습니다."));

			assertAll(
				() -> assertThat(enrollment.getState()).isEqualTo(EnrollmentState.PENDING),
				() -> assertThat(enrollment.getCourse().getId()).isEqualTo(course.getId()),
				() -> assertThat(enrollment.getUser().getId()).isEqualTo(user.getId()),
				() -> assertThat(savedCourse.getCapacity().getCurrent()).isEqualTo(1)
			);
		}

		@Test
		void 강의가_없다면_예외를_반환한다() {
			// given
			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());

			// when & then
			assertThatThrownBy(() -> enrollmentService.apply(999L, user.getId()))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 사용자가_없다면_예외를_반환한다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator);

			// when & then
			assertThatThrownBy(() -> enrollmentService.apply(course.getId(), 999L))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 모집중인_강의가_아니라면_예외를_반환한다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			Course course = courseRepository.save(CourseFixture.COURSE_FIXTURE_1.create(creator));

			// when & then
			assertThatThrownBy(() -> enrollmentService.apply(course.getId(), user.getId()))
				.isInstanceOf(ConflictException.class);
		}

		@Test
		void 정원이_가득_찼다면_예외를_반환한다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			User firstUser = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			User secondUser = userRepository.save(UserFixture.USER_FIXTURE_3.create());
			Course course = createSavedOpenCourseWithCapacityOne(creator);

			enrollmentService.apply(course.getId(), firstUser.getId());

			// when & then
			assertThatThrownBy(() -> enrollmentService.apply(course.getId(), secondUser.getId()))
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("수강 확정 기능")
	class ConfirmEnrollmentTest {
		@Test
		void 본인의_결제_대기_상태_수강_신청을_확정한다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator);

			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			Long enrollmentId = enrollmentService.apply(course.getId(), user.getId());

			// when
			enrollmentService.confirm(enrollmentId, user.getId());

			// then
			Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new AssertionError("수강 신청이 저장되지 않았습니다."));

			assertAll(
				() -> assertThat(enrollment.getState()).isEqualTo(EnrollmentState.CONFIRMED),
				() -> assertThat(enrollment.getPaidAt()).isNotNull()
			);
		}

		@Test
		void 본인의_수강_신청이_아니라면_예외를_반환한다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator);

			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			User otherUser = userRepository.save(UserFixture.USER_FIXTURE_3.create());

			Long enrollmentId = enrollmentRepository.save(Enrollment.apply(course, user)).getId();

			// when & then
			assertThatThrownBy(() -> enrollmentService.confirm(enrollmentId, otherUser.getId()))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		void 수강_신청이_없다면_예외를_반환한다() {
			// given
			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());

			// when & then
			assertThatThrownBy(() -> enrollmentService.confirm(999L, user.getId()))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 이미_결제_확정된_수강_신청은_다시_확정할_수_없다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator);

			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			Long enrollmentId = enrollmentRepository.save(Enrollment.apply(course, user)).getId();

			enrollmentService.confirm(enrollmentId, user.getId());

			// when & then
			assertThatThrownBy(() -> enrollmentService.confirm(enrollmentId, user.getId()))
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("수강 취소 기능")
	class CancelEnrollmentTest {
		@Test
		void 본인의_결제_대기_상태_수강_신청을_취소한다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator);

			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			Long enrollmentId = enrollmentService.apply(course.getId(), user.getId());

			// when
			enrollmentService.cancel(enrollmentId, user.getId());

			// then
			Course savedCourse = courseRepository.findById(course.getId())
				.orElseThrow(() -> new AssertionError("강의가 저장되지 않았습니다."));

			assertAll(
				() -> assertThat(enrollmentRepository.findById(enrollmentId)).isEmpty(),
				() -> assertThat(savedCourse.getCapacity().getCurrent()).isZero()
			);
		}

		@Test
		void 본인의_수강_신청이_아니라면_예외를_반환한다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator);

			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			Long enrollmentId = enrollmentService.apply(course.getId(), user.getId());

			User otherUser = userRepository.save(UserFixture.USER_FIXTURE_3.create());

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancel(enrollmentId, otherUser.getId()))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		void 수강_신청이_없다면_예외를_반환한다() {
			// given
			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancel(999L, user.getId()))
				.isInstanceOf(NotFoundException.class);
		}
	}

	@Nested
	@DisplayName("수강 확정 취소 기능")
	class CancelConfirmedEnrollmentTest {
		@Test
		void 본인의_결제_확정_상태_수강_신청을_취소한다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator);

			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			Enrollment enrollment = createSavedConfirmedEnrollment(course, user, LocalDateTime.now());

			// when
			enrollmentService.cancelConfirm(enrollment.getId(), user.getId());

			// then
			Enrollment savedEnrollment = enrollmentRepository.findById(enrollment.getId())
				.orElseThrow(() -> new AssertionError("수강 신청이 저장되지 않았습니다."));
			Course savedCourse = courseRepository.findById(course.getId())
				.orElseThrow(() -> new AssertionError("강의가 저장되지 않았습니다."));

			assertAll(
				() -> assertThat(savedEnrollment.getState()).isEqualTo(EnrollmentState.CANCELLED),
				() -> assertThat(savedEnrollment.getCancelledAt()).isNotNull(),
				() -> assertThat(savedCourse.getCapacity().getCurrent()).isZero()
			);
		}

		@Test
		void 본인의_수강_신청이_아니라면_수강_확정_취소_할_수_없다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator);

			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			User otherUser = userRepository.save(UserFixture.USER_FIXTURE_3.create());
			Enrollment enrollment = createSavedConfirmedEnrollment(course, user, LocalDateTime.now());

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancelConfirm(enrollment.getId(), otherUser.getId()))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		void 수강_신청이_없다면_수강_확정_취소_할_수_없다() {
			// given
			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancelConfirm(999L, user.getId()))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 결제_대기_상태의_수강_신청은_수강_확정_취소_할_수_없다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator);

			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			Enrollment enrollment = enrollmentRepository.save(Enrollment.apply(course, user));

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancelConfirm(enrollment.getId(), user.getId()))
				.isInstanceOf(ConflictException.class);
		}

		@Test
		void 결제_후_7일이_지나면_수강_확정_취소_할_수_없다() {
			// given
			User creator = userRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
			Course course = createSavedOpenCourse(creator);

			User user = userRepository.save(UserFixture.USER_FIXTURE_2.create());
			Enrollment enrollment = createSavedConfirmedEnrollment(course, user, LocalDateTime.now().minusDays(8));

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancelConfirm(enrollment.getId(), user.getId()))
				.isInstanceOf(BadRequestException.class);
		}
	}

	private Enrollment createSavedConfirmedEnrollment(Course course, User user, LocalDateTime paidAt) {
		Enrollment enrollment = enrollmentRepository.save(Enrollment.apply(course, user));
		enrollment.confirmPayment(paidAt);
		return enrollment;
	}

	private Course createSavedOpenCourse(User creator) {
		Course course = courseRepository.save(CourseFixture.COURSE_FIXTURE_1.create(creator));
		course.open();
		return course;
	}

	private Course createSavedOpenCourseWithCapacityOne(User creator) {
		Course course = courseRepository.save(Course.of(
			"정원 1명 강의",
			"수강 신청 정원 초과 테스트용 강의입니다.",
			100_000,
			new Capacity(1),
			LocalDate.of(2026, 6, 1),
			LocalDate.of(2026, 6, 30),
			creator
		));
		course.open();
		return course;
	}
}
