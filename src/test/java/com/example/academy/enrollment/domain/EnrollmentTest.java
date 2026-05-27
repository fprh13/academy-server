package com.example.academy.enrollment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.academy.common.exception.BadRequestException;
import com.example.academy.common.exception.ConflictException;
import com.example.academy.course.domain.Capacity;
import com.example.academy.course.domain.Course;
import com.example.academy.identity.domain.user.User;
import com.example.academy.support.fixture.CourseFixture;
import com.example.academy.support.fixture.UserFixture;

class EnrollmentTest {
	private static final LocalDateTime PAID_AT = LocalDateTime.of(2026, 6, 1, 10, 0);

	@Nested
	@DisplayName("수강 신청")
	class ApplyEnrollment {
		@Test
		void 수강_신청을_하면_PENDING_상태이고_신청_인원이_증가한다() {
			// given
			Course course = openCourse();
			User user = UserFixture.USER_FIXTURE_2.create();

			// when
			Enrollment enrollment = Enrollment.apply(course, user);

			// then
			assertThat(enrollment.isPending()).isTrue();
			assertThat(enrollment.getCourse()).isSameAs(course);
			assertThat(enrollment.getUser()).isSameAs(user);
			assertThat(course.getCapacity().getCurrent()).isEqualTo(1);
		}

		@Test
		void 모집_중이_아니면_수강_신청할_수_없다() {
			// given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);
			User user = UserFixture.USER_FIXTURE_2.create();

			// when & then
			assertThatThrownBy(() -> Enrollment.apply(course, user))
				.isInstanceOf(ConflictException.class);
		}

		@Test
		void 정원이_가득_찼다면_수강_신청할_수_없다() {
			// given
			Course course = openCourseWithCapacityOne();
			User firstUser = UserFixture.USER_FIXTURE_2.create();
			User secondUser = UserFixture.USER_FIXTURE_3.create();
			Enrollment.apply(course, firstUser);

			// when & then
			assertThatThrownBy(() -> Enrollment.apply(course, secondUser))
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("결제 확정")
	class ConfirmPayment {
		@Test
		void 결제_대기_상태의_수강_신청은_결제를_확정할_수_있다() {
			// given
			Enrollment enrollment = createPendingEnrollment();

			// when
			enrollment.confirmPayment(PAID_AT);

			// then
			assertThat(enrollment.isConfirmed()).isTrue();
			assertThat(enrollment.getPaidAt()).isEqualTo(PAID_AT);
		}

		@Test
		void 결제_완료_상태라면_동일한_결제를_다시_할_수_없다() {
			// given
			Enrollment enrollment = createConfirmedEnrollment();

			// when & then
			assertThatThrownBy(() -> enrollment.confirmPayment(PAID_AT.plusHours(1)))
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("수강 신청 취소")
	class CancelApplication {
		@Test
		void 취소하면_신청_인원이_감소한다() {
			// given
			Course course = openCourse();
			Enrollment enrollment = Enrollment.apply(course, UserFixture.USER_FIXTURE_2.create());

			// when
			enrollment.cancelApplication();

			// then
			assertThat(course.getCapacity().getCurrent()).isZero();
		}

		@Test
		void 결제_대기_상태의_수강_신청은_취소할_수_있다() {
			// given
			Course course = openCourse();
			Enrollment enrollment = Enrollment.apply(course, UserFixture.USER_FIXTURE_2.create());

			// when
			enrollment.cancelApplication();

			// then
			assertThat(enrollment.isPending()).isTrue();
			assertThat(enrollment.getCancelledAt()).isNull();
			assertThat(course.getCapacity().getCurrent()).isZero();
		}

		@Test
		void 결제_확정_상태의_수강_신청_취소할_수_없다() {
			// given
			Enrollment enrollment = createConfirmedEnrollment();

			// when & then
			assertThatThrownBy(enrollment::cancelApplication)
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("수강 확정 취소")
	class CancelConfirmedEnrollment {
		@Test
		void 취소하면_신청_인원이_감소한다() {
			// given
			Course course = openCourse();
			Enrollment enrollment = Enrollment.apply(course, UserFixture.USER_FIXTURE_2.create());
			enrollment.confirmPayment(PAID_AT);

			// when
			enrollment.cancelConfirmed(PAID_AT.plusDays(1));

			// then
			assertThat(course.getCapacity().getCurrent()).isZero();
		}

		@Test
		void 결제_확정_상태라면_취소할_수_있다() {
			// given
			Course course = openCourse();
			Enrollment enrollment = Enrollment.apply(course, UserFixture.USER_FIXTURE_2.create());
			enrollment.confirmPayment(PAID_AT);
			LocalDateTime cancelledAt = PAID_AT.plusDays(7);

			// when
			enrollment.cancelConfirmed(cancelledAt);

			// then
			assertThat(enrollment.isCancelled()).isTrue();
			assertThat(enrollment.getCancelledAt()).isEqualTo(cancelledAt);
			assertThat(course.getCapacity().getCurrent()).isZero();
		}

		@Test
		void 결제_대기_상태의_수강_신청은_결제_취소를_할_수_없다() {
			// given
			Enrollment enrollment = createPendingEnrollment();

			// when & then
			assertThatThrownBy(() -> enrollment.cancelConfirmed(PAID_AT.plusDays(1)))
				.isInstanceOf(ConflictException.class);
		}

		@Test
		void 결제_후_7일이_지나면_수강_확정을_취소할_수_없다() {
			// given
			Enrollment enrollment = createConfirmedEnrollment();

			// when & then
			assertThatThrownBy(() -> enrollment.cancelConfirmed(PAID_AT.plusDays(7).plusSeconds(1)))
				.isInstanceOf(BadRequestException.class);
		}

		@Test
		void 이미_취소된_결제는_다시_취소할_수_없다() {
			// given
			Enrollment enrollment = createConfirmedEnrollment();
			enrollment.cancelConfirmed(PAID_AT.plusDays(1));

			// when & then
			assertThatThrownBy(() -> enrollment.cancelConfirmed(PAID_AT.plusDays(2)))
				.isInstanceOf(ConflictException.class);
		}
	}

	private static Course openCourse() {
		User creator = UserFixture.USER_FIXTURE_1.createCreator();
		Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);
		course.open();
		return course;
	}

	private static Course openCourseWithCapacityOne() {
		User creator = UserFixture.USER_FIXTURE_1.createCreator();
		Course course = Course.of(
			"정원 1명 강의",
			"마지막 자리 테스트용 강의입니다.",
			100_000,
			new Capacity(1),
			LocalDate.of(2026, 6, 1),
			LocalDate.of(2026, 6, 30),
			creator
		);
		course.open();
		return course;
	}

	private static Enrollment createPendingEnrollment() {
		return Enrollment.apply(openCourse(), UserFixture.USER_FIXTURE_2.create());
	}

	private static Enrollment createConfirmedEnrollment() {
		Enrollment enrollment = createPendingEnrollment();
		enrollment.confirmPayment(PAID_AT);
		return enrollment;
	}
}
