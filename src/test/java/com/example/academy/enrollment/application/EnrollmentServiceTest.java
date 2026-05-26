package com.example.academy.enrollment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.course.domain.Course;
import com.example.academy.course.domain.CourseRepository;
import com.example.academy.enrollment.domain.Enrollment;
import com.example.academy.enrollment.domain.EnrollmentRepository;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.support.fixture.CourseFixture;
import com.example.academy.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

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

			Mockito.when(courseRepository.findById(course.getId()))
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

			Mockito.when(courseRepository.findById(course.getId()))
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

			Mockito.when(courseRepository.findById(course.getId()))
				.thenReturn(Optional.of(course));
			Mockito.when(userRepository.findById(user.getId()))
				.thenReturn(Optional.of(user));
			Mockito.when(enrollmentRepository.save(any(Enrollment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			enrollmentService.apply(course.getId(), user.getId());

			// then
			Mockito.verify(courseRepository, Mockito.times(1))
				.findById(course.getId());
		}

		@Test
		void 사용자를_조회한다() {
			// given
			Course course = createOpenCourse(1L);
			User user = createUser(2L);

			Mockito.when(courseRepository.findById(course.getId()))
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

			Mockito.when(courseRepository.findById(course.getId()))
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
	}

	private Enrollment createPendingEnrollment(Long enrollmentId, Long userId) {
		Course course = createOpenCourse(1L);
		User user = createUser(userId);
		Enrollment enrollment = Enrollment.apply(course, user);
		ReflectionTestUtils.setField(enrollment, "id", enrollmentId);
		return enrollment;
	}

	private Course createOpenCourse(Long courseId) {
		User creator = UserFixture.USER_FIXTURE_1.createCreator();
		Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);
		course.open();
		ReflectionTestUtils.setField(course, "id", courseId);
		return course;
	}

	private User createUser(Long userId) {
		User user = UserFixture.USER_FIXTURE_2.create();
		ReflectionTestUtils.setField(user, "id", userId);
		return user;
	}

}
