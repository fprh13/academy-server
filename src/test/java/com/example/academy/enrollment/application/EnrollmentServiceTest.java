package com.example.academy.enrollment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.academy.common.exception.ConflictException;
import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.common.presentation.dto.PagingRequest;
import com.example.academy.common.presentation.dto.PagingResponse;
import com.example.academy.course.domain.Capacity;
import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseRepository;
import com.example.academy.enrollment.domain.Enrollment;
import com.example.academy.enrollment.domain.EnrollmentRepository;
import com.example.academy.enrollment.presentation.dto.response.EnrollmentInfoResponse;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.support.fixture.CourseFixture;
import com.example.academy.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {
	private static final LocalDateTime PAID_AT = LocalDateTime.of(2026, 6, 1, 10, 0);

	@InjectMocks
	private EnrollmentService enrollmentService;

	@Mock
	private EnrollmentRepository enrollmentRepository;

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private UserRepository userRepository;

	@Nested
	@DisplayName("수강 신청")
	class ApplyEnrollment {
		@Test
		void 수강_신청을_완료하면_PK를_반환한다() {
			// given
			Course course = createOpenCourse(1L);
			User user = createUser(2L);

			Mockito.when(courseRepository.findByIdForUpdate(course.getId()))
				.thenReturn(Optional.of(course));
			Mockito.when(userRepository.findById(user.getId()))
				.thenReturn(Optional.of(user));
			Mockito.when(enrollmentRepository.save(any(Enrollment.class)))
				.thenAnswer(invocation -> {
					Enrollment enrollment = invocation.getArgument(0);
					ReflectionTestUtils.setField(enrollment, "id", 10L);
					return enrollment;
				});

			// when
			Long enrollmentId = enrollmentService.apply(course.getId(), user.getId());

			// then
			assertThat(enrollmentId).isEqualTo(10L);
		}

		@Test
		void 수강_신청을_완료하면_강의의_수강신청_인원을_증가시킨다() {
			// given
			Course course = createOpenCourse(1L);
			User user = createUser(2L);

			Mockito.when(courseRepository.findByIdForUpdate(course.getId()))
				.thenReturn(Optional.of(course));
			Mockito.when(userRepository.findById(user.getId()))
				.thenReturn(Optional.of(user));
			Mockito.when(enrollmentRepository.save(any(Enrollment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			enrollmentService.apply(course.getId(), user.getId());

			// then
			assertThat(course.getCapacity().getCurrent()).isEqualTo(1);
		}

		@Test
		void 강의를_조회한다() {
			// given
			Course course = createOpenCourse(1L);
			User user = createUser(2L);

			Mockito.when(courseRepository.findByIdForUpdate(course.getId()))
				.thenReturn(Optional.of(course));
			Mockito.when(userRepository.findById(user.getId()))
				.thenReturn(Optional.of(user));
			Mockito.when(enrollmentRepository.save(any(Enrollment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			enrollmentService.apply(course.getId(), user.getId());

			// then
			Mockito.verify(courseRepository, Mockito.times(1))
				.findByIdForUpdate(course.getId());
		}

		@Test
		void 사용자를_조회한다() {
			// given
			Course course = createOpenCourse(1L);
			User user = createUser(2L);

			Mockito.when(courseRepository.findByIdForUpdate(course.getId()))
				.thenReturn(Optional.of(course));
			Mockito.when(userRepository.findById(user.getId()))
				.thenReturn(Optional.of(user));
			Mockito.when(enrollmentRepository.save(any(Enrollment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			enrollmentService.apply(course.getId(), user.getId());

			// then
			Mockito.verify(userRepository, Mockito.times(1))
				.findById(user.getId());
		}

		@Test
		void 수강_신청을_한다() {
			// given
			Course course = createOpenCourse(1L);
			User user = createUser(2L);

			Mockito.when(courseRepository.findByIdForUpdate(course.getId()))
				.thenReturn(Optional.of(course));
			Mockito.when(userRepository.findById(user.getId()))
				.thenReturn(Optional.of(user));
			Mockito.when(enrollmentRepository.save(any(Enrollment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			enrollmentService.apply(course.getId(), user.getId());

			// then
			ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.save(enrollmentCaptor.capture());

			Enrollment enrollment = enrollmentCaptor.getValue();
			assertThat(enrollment.isPending()).isTrue();
			assertThat(enrollment.getCourse()).isSameAs(course);
			assertThat(enrollment.getUser()).isSameAs(user);
		}

		@Test
		void 정원이_가득_찼다면_웨이팅_상태의_수강_신청을_저장한다() {
			// given
			Course course = createOpenCourseWithCapacityOne(1L);
			User firstUser = createUser(2L);
			User secondUser = createUser(3L);
			Enrollment.apply(course, firstUser);

			Mockito.when(courseRepository.findByIdForUpdate(course.getId()))
				.thenReturn(Optional.of(course));
			Mockito.when(userRepository.findById(secondUser.getId()))
				.thenReturn(Optional.of(secondUser));
			Mockito.when(enrollmentRepository.save(any(Enrollment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			enrollmentService.apply(course.getId(), secondUser.getId());

			// then
			ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.save(enrollmentCaptor.capture());

			Enrollment enrollment = enrollmentCaptor.getValue();
			assertThat(enrollment.isWaiting()).isTrue();
			assertThat(enrollment.getCourse()).isSameAs(course);
			assertThat(enrollment.getUser()).isSameAs(secondUser);
			assertThat(course.getCapacity().getCurrent()).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("수강 확정")
	class ConfirmEnrollment {
		@Test
		void 본인의_결제_대기_상태_수강_신청을_확정한다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;

			Enrollment enrollment = createPendingEnrollment(enrollmentId, userId);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));

			// when
			enrollmentService.confirm(enrollmentId, userId);

			// then
			assertThat(enrollment.isConfirmed()).isTrue();
			assertThat(enrollment.getPaidAt()).isNotNull();
		}

		@Test
		void 수강_신청을_조회한다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Enrollment enrollment = createPendingEnrollment(enrollmentId, userId);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));

			// when
			enrollmentService.confirm(enrollmentId, userId);

			// then
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.findById(enrollmentId);
		}

		@Test
		void 본인의_수강_신청이_아니면_확정할_수_없다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Long otherUserId = 3L;

			Enrollment enrollment = createPendingEnrollment(enrollmentId, userId);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));

			// when & then
			assertThatThrownBy(() -> enrollmentService.confirm(enrollmentId, otherUserId))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		void 수강_신청이_없으면_확정할_수_없다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> enrollmentService.confirm(enrollmentId, userId))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 웨이팅_상태의_수강_신청은_확정할_수_없다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Enrollment enrollment = createWaitingEnrollment(enrollmentId, userId);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));

			// when & then
			assertThatThrownBy(() -> enrollmentService.confirm(enrollmentId, userId))
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("수강 신청 취소")
	class CancelEnrollment {
		@Test
		void 본인의_결제_대기_상태_수강_신청을_취소한다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Enrollment enrollment = createPendingEnrollment(enrollmentId, userId);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));
			Mockito.when(courseRepository.findByIdForUpdate(enrollment.getCourse().getId()))
				.thenReturn(Optional.of(enrollment.getCourse()));
			Mockito.when(enrollmentRepository.findOldestWaitingByCourseIdForUpdate(enrollment.getCourse().getId()))
				.thenReturn(Optional.empty());

			// when
			enrollmentService.cancel(enrollmentId, userId);

			// then
			assertThat(enrollment.isPending()).isTrue();
			assertThat(enrollment.getCancelledAt()).isNull();
			assertThat(enrollment.getCourse().getCapacity().getCurrent()).isZero();
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.findById(enrollmentId);
			Mockito.verify(courseRepository, Mockito.times(1))
				.findByIdForUpdate(enrollment.getCourse().getId());
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.findOldestWaitingByCourseIdForUpdate(enrollment.getCourse().getId());
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.deleteById(enrollmentId);
		}

		@Test
		void 수강_신청을_취소하면_가장_오래된_웨이팅_수강_신청을_결제_대기_상태로_승격한다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Course course = createOpenCourseWithCapacityOne(1L);
			Enrollment enrollment = Enrollment.apply(course, createUser(userId));
			Enrollment waitingEnrollment = Enrollment.apply(course, createUser(3L));
			ReflectionTestUtils.setField(enrollment, "id", enrollmentId);
			ReflectionTestUtils.setField(waitingEnrollment, "id", 11L);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));
			Mockito.when(courseRepository.findByIdForUpdate(course.getId()))
				.thenReturn(Optional.of(course));
			Mockito.when(enrollmentRepository.findOldestWaitingByCourseIdForUpdate(course.getId()))
				.thenReturn(Optional.of(waitingEnrollment));

			// when
			enrollmentService.cancel(enrollmentId, userId);

			// then
			assertAll(
				() -> assertThat(waitingEnrollment.isPending()).isTrue(),
				() -> assertThat(course.getCapacity().getCurrent()).isEqualTo(1)
			);
			Mockito.verify(courseRepository, Mockito.times(1))
				.findByIdForUpdate(course.getId());
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.findOldestWaitingByCourseIdForUpdate(course.getId());
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.deleteById(enrollmentId);
		}

		@Test
		void 본인의_수강_신청이_아니면_취소할_수_없다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Long otherUserId = 3L;
			Enrollment enrollment = createPendingEnrollment(enrollmentId, userId);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancel(enrollmentId, otherUserId))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		void 수강_신청이_없으면_취소할_수_없다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancel(enrollmentId, userId))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 웨이팅_상태의_수강_신청은_취소할_수_없다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Enrollment enrollment = createWaitingEnrollment(enrollmentId, userId);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));
			Mockito.when(courseRepository.findByIdForUpdate(enrollment.getCourse().getId()))
				.thenReturn(Optional.of(enrollment.getCourse()));

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancel(enrollmentId, userId))
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("수강 확정 취소")
	class CancelConfirmedEnrollment {
		@Test
		void 본인의_결제_확정_상태_수강_신청을_취소한다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Enrollment enrollment = createConfirmedEnrollment(enrollmentId, userId, PAID_AT);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));
			Mockito.when(courseRepository.findByIdForUpdate(enrollment.getCourse().getId()))
				.thenReturn(Optional.of(enrollment.getCourse()));
			Mockito.when(enrollmentRepository.findOldestWaitingByCourseIdForUpdate(enrollment.getCourse().getId()))
				.thenReturn(Optional.empty());

			// when
			enrollmentService.cancelConfirm(enrollmentId, userId);

			// then
			assertThat(enrollment.isCancelled()).isTrue();
			assertThat(enrollment.getCancelledAt()).isNotNull();
			assertThat(enrollment.getCourse().getCapacity().getCurrent()).isZero();
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.findById(enrollmentId);
			Mockito.verify(courseRepository, Mockito.times(1))
				.findByIdForUpdate(enrollment.getCourse().getId());
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.findOldestWaitingByCourseIdForUpdate(enrollment.getCourse().getId());
		}

		@Test
		void 수강_확정_취소를_하면_가장_오래된_웨이팅_수강_신청을_결제_대기_상태로_승격한다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Course course = createOpenCourseWithCapacityOne(1L);
			Enrollment enrollment = Enrollment.apply(course, createUser(userId));
			enrollment.confirmPayment(PAID_AT);
			Enrollment waitingEnrollment = Enrollment.apply(course, createUser(3L));
			ReflectionTestUtils.setField(enrollment, "id", enrollmentId);
			ReflectionTestUtils.setField(waitingEnrollment, "id", 11L);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));
			Mockito.when(courseRepository.findByIdForUpdate(course.getId()))
				.thenReturn(Optional.of(course));
			Mockito.when(enrollmentRepository.findOldestWaitingByCourseIdForUpdate(course.getId()))
				.thenReturn(Optional.of(waitingEnrollment));

			// when
			enrollmentService.cancelConfirm(enrollmentId, userId);

			// then
			assertAll(
				() -> assertThat(enrollment.isCancelled()).isTrue(),
				() -> assertThat(waitingEnrollment.isPending()).isTrue(),
				() -> assertThat(course.getCapacity().getCurrent()).isEqualTo(1)
			);
			Mockito.verify(courseRepository, Mockito.times(1))
				.findByIdForUpdate(course.getId());
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.findOldestWaitingByCourseIdForUpdate(course.getId());
		}

		@Test
		void 수강_확정_취소_대상의_수강_신청을_조회한다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Enrollment enrollment = createConfirmedEnrollment(enrollmentId, userId, PAID_AT);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));
			Mockito.when(courseRepository.findByIdForUpdate(enrollment.getCourse().getId()))
				.thenReturn(Optional.of(enrollment.getCourse()));
			Mockito.when(enrollmentRepository.findOldestWaitingByCourseIdForUpdate(enrollment.getCourse().getId()))
				.thenReturn(Optional.empty());

			// when
			enrollmentService.cancelConfirm(enrollmentId, userId);

			// then
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.findById(enrollmentId);
			Mockito.verify(courseRepository, Mockito.times(1))
				.findByIdForUpdate(enrollment.getCourse().getId());
		}

		@Test
		void 본인의_수강_신청이_아니면_수강_확정을_취소할_수_없다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;
			Long otherUserId = 3L;
			Enrollment enrollment = createConfirmedEnrollment(enrollmentId, userId, PAID_AT);

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.of(enrollment));

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancelConfirm(enrollmentId, otherUserId))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		void 수강_신청이_없으면_수강_확정을_취소할_수_없다() {
			// given
			Long enrollmentId = 10L;
			Long userId = 2L;

			Mockito.when(enrollmentRepository.findById(enrollmentId))
				.thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancelConfirm(enrollmentId, userId))
				.isInstanceOf(NotFoundException.class);
		}
	}

	@Nested
	@DisplayName("수강 신청 목록 조회")
	class GetEnrollments {
		@Test
		void 페이지_요청_기본값을_적용해_목록을_조회한다() {
			// given
			Long userId = 2L;
			PagingRequest request = new PagingRequest(null, null, null);
			Page<Enrollment> enrollmentPage = new PageImpl<>(List.of());

			Mockito.when(enrollmentRepository.findPageByUserIdAndStateIn(userId, null, 0, 10, "createAt"))
				.thenReturn(enrollmentPage);

			// when
			enrollmentService.gets(request, null, userId);

			// then
			Mockito.verify(enrollmentRepository, Mockito.times(1))
				.findPageByUserIdAndStateIn(userId, null, 0, 10, "createAt");
		}

		@Test
		void 수강_신청_목록을_페이지_응답으로_변환한다() {
			// given
			Long userId = 2L;
			String state = "confirmed";
			PagingRequest request = new PagingRequest(1, 10, null);

			User firstCreator = createCreator(11L, UserFixture.USER_FIXTURE_1);
			User secondCreator = createCreator(12L, UserFixture.USER_FIXTURE_3);

			Course firstCourse = createOpenCourse(101L, firstCreator, CourseFixture.COURSE_FIXTURE_1);
			Course secondCourse = createOpenCourse(102L, secondCreator, CourseFixture.COURSE_FIXTURE_2);

			LocalDateTime firstCreatedAt = LocalDateTime.of(2026, 6, 2, 10, 0);
			LocalDateTime secondCreatedAt = LocalDateTime.of(2026, 6, 3, 11, 30);

			Enrollment pendingEnrollment = createPendingEnrollment(1001L, userId, firstCourse, firstCreatedAt);
			Enrollment confirmedEnrollment = createConfirmedEnrollment(1002L, userId, secondCourse, secondCreatedAt, PAID_AT);

			Page<Enrollment> enrollmentPage = new PageImpl<>(
				List.of(pendingEnrollment, confirmedEnrollment),
				PageRequest.of(0, 10),
				3
			);

			Mockito.when(enrollmentRepository.findPageByUserIdAndStateIn(userId, state, 0, 10, "createAt"))
				.thenReturn(enrollmentPage);

			// when
			PagingResponse<EnrollmentInfoResponse> response = enrollmentService.gets(request, state, userId);

			// then
			assertThat(response.content()).hasSize(2);

			EnrollmentInfoResponse firstResponse = response.content().get(0);
			assertAll(
				() -> assertThat(firstResponse.enrollmentId()).isEqualTo(1001L),
				() -> assertThat(firstResponse.state()).isEqualTo("PENDING"),
				() -> assertThat(firstResponse.createAt()).isEqualTo(firstCreatedAt),
				() -> assertThat(firstResponse.paidAt()).isNull(),
				() -> assertThat(firstResponse.courseInfo().courseId()).isEqualTo(101L),
				() -> assertThat(firstResponse.courseInfo().courseName()).isEqualTo(firstCreator.getName()),
				() -> assertThat(firstResponse.courseInfo().coursePrice()).isEqualTo(firstCourse.getPrice())
			);

			EnrollmentInfoResponse secondResponse = response.content().get(1);
			assertAll(
				() -> assertThat(secondResponse.enrollmentId()).isEqualTo(1002L),
				() -> assertThat(secondResponse.state()).isEqualTo("CONFIRMED"),
				() -> assertThat(secondResponse.createAt()).isEqualTo(secondCreatedAt),
				() -> assertThat(secondResponse.paidAt()).isEqualTo(PAID_AT),
				() -> assertThat(secondResponse.courseInfo().courseId()).isEqualTo(102L),
				() -> assertThat(secondResponse.courseInfo().courseName()).isEqualTo(secondCreator.getName()),
				() -> assertThat(secondResponse.courseInfo().coursePrice()).isEqualTo(secondCourse.getPrice())
			);

			assertAll(
				() -> assertThat(response.page().number()).isEqualTo(1),
				() -> assertThat(response.page().size()).isEqualTo(10),
				() -> assertThat(response.page().totalElements()).isEqualTo(2),
				() -> assertThat(response.page().totalPages()).isEqualTo(1),
				() -> assertThat(response.page().hasNext()).isFalse(),
				() -> assertThat(response.page().hasPrevious()).isFalse()
			);
		}
	}

	private Enrollment createPendingEnrollment(Long enrollmentId, Long userId) {
		Course course = createOpenCourse(1L);
		User user = createUser(userId);
		Enrollment enrollment = Enrollment.apply(course, user);
		ReflectionTestUtils.setField(enrollment, "id", enrollmentId);
		return enrollment;
	}

	private Enrollment createPendingEnrollment(Long enrollmentId, Long userId, Course course, LocalDateTime createdAt) {
		User user = createUser(userId);
		Enrollment enrollment = Enrollment.apply(course, user);
		ReflectionTestUtils.setField(enrollment, "id", enrollmentId);
		ReflectionTestUtils.setField(enrollment, "createAt", createdAt);
		return enrollment;
	}

	private Enrollment createConfirmedEnrollment(Long enrollmentId, Long userId, LocalDateTime paidAt) {
		Enrollment enrollment = createPendingEnrollment(enrollmentId, userId);
		enrollment.confirmPayment(paidAt);
		return enrollment;
	}

	private Enrollment createWaitingEnrollment(Long enrollmentId, Long userId) {
		Course course = createOpenCourseWithCapacityOne(1L);
		Enrollment.apply(course, createUser(999L));
		Enrollment enrollment = Enrollment.apply(course, createUser(userId));
		ReflectionTestUtils.setField(enrollment, "id", enrollmentId);
		return enrollment;
	}

	private Enrollment createConfirmedEnrollment(
		Long enrollmentId,
		Long userId,
		Course course,
		LocalDateTime createdAt,
		LocalDateTime paidAt
	) {
		Enrollment enrollment = createPendingEnrollment(enrollmentId, userId, course, createdAt);
		enrollment.confirmPayment(paidAt);
		return enrollment;
	}

	private Course createOpenCourse(Long courseId) {
		return createOpenCourse(courseId, createCreator(1L, UserFixture.USER_FIXTURE_1), CourseFixture.COURSE_FIXTURE_1);
	}

	private Course createOpenCourse(Long courseId, User creator, CourseFixture courseFixture) {
		Course course = courseFixture.create(creator);
		course.open();
		ReflectionTestUtils.setField(course, "id", courseId);
		return course;
	}

	private Course createOpenCourseWithCapacityOne(Long courseId) {
		User creator = createCreator(1L, UserFixture.USER_FIXTURE_1);
		Course course = Course.of(
			"정원 1명 강의",
			"웨이팅 테스트용 강의입니다.",
			100_000,
			new Capacity(1),
			LocalDate.of(2026, 6, 1),
			LocalDate.of(2026, 6, 30),
			creator
		);
		course.open();
		ReflectionTestUtils.setField(course, "id", courseId);
		return course;
	}

	private User createCreator(Long creatorId, UserFixture userFixture) {
		User creator = userFixture.createCreator();
		ReflectionTestUtils.setField(creator, "id", creatorId);
		return creator;
	}

	private User createUser(Long userId) {
		User user = UserFixture.USER_FIXTURE_2.create();
		ReflectionTestUtils.setField(user, "id", userId);
		return user;
	}

}
