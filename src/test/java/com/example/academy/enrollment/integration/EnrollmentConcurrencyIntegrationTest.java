package com.example.academy.enrollment.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.academy.course.domain.Capacity;
import com.example.academy.course.domain.Course;
import com.example.academy.course.infrastructure.JpaCourseRepository;
import com.example.academy.enrollment.application.EnrollmentService;
import com.example.academy.enrollment.domain.Enrollment;
import com.example.academy.enrollment.domain.EnrollmentState;
import com.example.academy.enrollment.infrastructure.JpaEnrollmentRepository;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.infrastructure.persistence.JpaUserRepository;
import com.example.academy.support.IntegrationSupportTest;
import com.example.academy.support.fixture.UserFixture;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class EnrollmentConcurrencyIntegrationTest extends IntegrationSupportTest {

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private JpaEnrollmentRepository jpaEnrollmentRepository;

	@Autowired
	private JpaCourseRepository jpaCourseRepository;

	@Autowired
	private JpaUserRepository jpaUserRepository;

	@AfterEach
	void tearDown() {
		jpaEnrollmentRepository.deleteAllInBatch();
		jpaCourseRepository.deleteAllInBatch();
		jpaUserRepository.deleteAllInBatch();
	}

	@Test
	@DisplayName("정원 1명 강의에 대한 동시 신청은 마지막 자리를 한 명에게만 배정한다")
	void apply_동시요청은_마지막_자리를_한명에게만_배정한다() throws Exception {
		// given
		User creator = jpaUserRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
		Course course = createOpenCourseWithCapacityOne(creator);
		User firstApplicant = jpaUserRepository.save(UserFixture.USER_FIXTURE_2.create());
		User secondApplicant = jpaUserRepository.save(UserFixture.USER_FIXTURE_3.create());

		// when
		ConcurrentApplyResult result = executeConcurrently(
			() -> enrollmentService.apply(course.getId(), firstApplicant.getId()),
			() -> enrollmentService.apply(course.getId(), secondApplicant.getId())
		);

		// then
		Course savedCourse = jpaCourseRepository.findById(course.getId())
			.orElseThrow(() -> new AssertionError("강의가 저장되지 않았습니다."));
		Enrollment firstEnrollment = jpaEnrollmentRepository.findById(result.firstEnrollmentId())
			.orElseThrow(() -> new AssertionError("첫 번째 수강 신청이 저장되지 않았습니다."));
		Enrollment secondEnrollment = jpaEnrollmentRepository.findById(result.secondEnrollmentId())
			.orElseThrow(() -> new AssertionError("두 번째 수강 신청이 저장되지 않았습니다."));
		List<Enrollment> enrollments = jpaEnrollmentRepository.findAll().stream()
			.filter(enrollment -> enrollment.getCourse().getId().equals(course.getId()))
			.toList();

		assertAll(
			() -> assertThat(savedCourse.getCapacity().getCurrent()).isEqualTo(1),
			() -> assertThat(enrollments).hasSize(2),
			() -> assertThat(countByState(enrollments, EnrollmentState.PENDING)).isEqualTo(1),
			() -> assertThat(countByState(enrollments, EnrollmentState.WAITING)).isEqualTo(1),
			() -> assertThat(List.of(firstEnrollment.getState(), secondEnrollment.getState()))
				.containsExactlyInAnyOrder(EnrollmentState.PENDING, EnrollmentState.WAITING)
		);
	}

	@Test
	@DisplayName("동시 취소 요청은 대기열 승격을 한 번만 수행한다")
	void 대기열_승격을_한번만_수행한다() throws Exception {
		// given
		User creator = jpaUserRepository.save(UserFixture.USER_FIXTURE_1.createCreator());
		Course course = createOpenCourseWithCapacityOne(creator);

		User applicant = jpaUserRepository.save(UserFixture.USER_FIXTURE_2.create());
		User firstWaitingUser = jpaUserRepository.save(UserFixture.USER_FIXTURE_3.create());
		User secondWaitingUser = jpaUserRepository.save(User.register("test4", "test4@1234", "test4@test.com", "김도"));

		Long pendingEnrollmentId = enrollmentService.apply(course.getId(), applicant.getId());
		Long firstWaitingEnrollmentId = enrollmentService.apply(course.getId(), firstWaitingUser.getId());
		Long secondWaitingEnrollmentId = enrollmentService.apply(course.getId(), secondWaitingUser.getId());

		// when
		ConcurrentExecutionResult result = executeConcurrently(
			() -> enrollmentService.cancel(pendingEnrollmentId, applicant.getId()),
			() -> enrollmentService.cancel(pendingEnrollmentId, applicant.getId())
		);

		// then
		Course savedCourse = jpaCourseRepository.findById(course.getId())
			.orElseThrow(() -> new AssertionError("강의가 저장되지 않았습니다."));
		List<Enrollment> enrollments = jpaEnrollmentRepository.findAll().stream()
			.filter(enrollment -> enrollment.getCourse().getId().equals(course.getId()))
			.toList();

		assertAll(
			() -> assertThat(result.successCount()).isEqualTo(1),
			() -> assertThat(result.failureCount()).isEqualTo(1),
			() -> assertThat(jpaEnrollmentRepository.findById(pendingEnrollmentId)).isEmpty(),
			() -> assertThat(savedCourse.getCapacity().getCurrent()).isEqualTo(1),
			() -> assertThat(enrollments).hasSize(2),
			() -> assertThat(countByState(enrollments, EnrollmentState.PENDING)).isEqualTo(1),
			() -> assertThat(countByState(enrollments, EnrollmentState.WAITING)).isEqualTo(1),
			() -> assertThat(jpaEnrollmentRepository.findById(firstWaitingEnrollmentId))
				.get()
				.extracting(Enrollment::getState)
				.isEqualTo(EnrollmentState.PENDING),
			() -> assertThat(jpaEnrollmentRepository.findById(secondWaitingEnrollmentId))
				.get()
				.extracting(Enrollment::getState)
				.isEqualTo(EnrollmentState.WAITING)
		);
	}

	private ConcurrentExecutionResult executeConcurrently(ThrowingRunnable firstAction, ThrowingRunnable secondAction)
		throws InterruptedException, ExecutionException {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		CountDownLatch readyLatch = new CountDownLatch(2);
		CountDownLatch startLatch = new CountDownLatch(1);

		try {
			Future<Boolean> firstFuture = executorService.submit(toCallable(firstAction, readyLatch, startLatch));
			Future<Boolean> secondFuture = executorService.submit(toCallable(secondAction, readyLatch, startLatch));

			readyLatch.await(5, TimeUnit.SECONDS);
			startLatch.countDown();

			int successCount = 0;
			int failureCount = 0;
			for (Future<Boolean> future : List.of(firstFuture, secondFuture)) {
				if (future.get()) {
					successCount++;
					continue;
				}
				failureCount++;
			}

			return new ConcurrentExecutionResult(successCount, failureCount);
		}
		finally {
			executorService.shutdownNow();
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	private ConcurrentApplyResult executeConcurrently(ThrowingSupplier<Long> firstAction, ThrowingSupplier<Long> secondAction)
		throws InterruptedException, ExecutionException {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		CountDownLatch readyLatch = new CountDownLatch(2);
		CountDownLatch startLatch = new CountDownLatch(1);

		try {
			Future<Long> firstFuture = executorService.submit(toCallable(firstAction, readyLatch, startLatch));
			Future<Long> secondFuture = executorService.submit(toCallable(secondAction, readyLatch, startLatch));

			readyLatch.await(5, TimeUnit.SECONDS);
			startLatch.countDown();

			return new ConcurrentApplyResult(firstFuture.get(), secondFuture.get());
		}
		finally {
			executorService.shutdownNow();
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	private Callable<Boolean> toCallable(
		ThrowingRunnable action,
		CountDownLatch readyLatch,
		CountDownLatch startLatch
	) {
		return () -> {
			readyLatch.countDown();
			startLatch.await();
			try {
				action.run();
				return true;
			}
			catch (Exception exception) {
				return false;
			}
		};
	}

	private Callable<Long> toCallable(
		ThrowingSupplier<Long> action,
		CountDownLatch readyLatch,
		CountDownLatch startLatch
	) {
		return () -> {
			readyLatch.countDown();
			startLatch.await();
			return action.get();
		};
	}

	private long countByState(List<Enrollment> enrollments, EnrollmentState state) {
		return enrollments.stream()
			.filter(enrollment -> enrollment.getState() == state)
			.count();
	}

	private Course createOpenCourseWithCapacityOne(User creator) {
		Course course = Course.of(
			"정원 1명 동시성 강의",
			"동시성 취소 테스트용 강의입니다.",
			100_000,
			new Capacity(1),
			java.time.LocalDate.of(2026, 6, 1),
			java.time.LocalDate.of(2026, 6, 30),
			creator
		);
		course.open();
		return jpaCourseRepository.save(course);
	}

	@FunctionalInterface
	interface ThrowingRunnable {
		void run() throws Exception;
	}

	@FunctionalInterface
	interface ThrowingSupplier<T> {
		T get() throws Exception;
	}

	record ConcurrentExecutionResult(int successCount, int failureCount) {
	}

	record ConcurrentApplyResult(Long firstEnrollmentId, Long secondEnrollmentId) {
	}
}
