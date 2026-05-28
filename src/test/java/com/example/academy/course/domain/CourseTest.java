package com.example.academy.course.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.academy.common.exception.BadRequestException;
import com.example.academy.common.exception.ConflictException;
import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.identity.domain.user.User;
import com.example.academy.support.fixture.CourseFixture;
import com.example.academy.support.fixture.UserFixture;

class CourseTest {

	@Nested
	@DisplayName("강의 생성")
	class CreateCourse {
		@Test
		void 강의를_생성하면_DRAFT_상태이다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);

			//when
			Course result = Course.of(
				course.getTitle(),
				course.getDescription(),
				course.getPrice(),
				course.getCapacity(),
				course.getStartDate(),
				course.getEndDate(),
				creator
			);

			//then
			assertThat(result.getState()).isEqualTo(CourseState.DRAFT);
		}

		@Test
		void 강사가_아니라면_강의를_생성할_수_없다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();

			//when & then
			assertThatThrownBy(() ->
				Course.of(
					"강사되는 법 마스터",
					"강사가 아니지만, 강의를 올려보는 실습을 해봅니다.",
					200_000,
					new Capacity(25),
					LocalDate.of(2026, 8, 1),
					LocalDate.of(2026, 8, 31),
					user
				)
			).isInstanceOf(ForbiddenException.class);
		}

		@Test
		void 가격이_음수라면_예외를_반환한다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();

			//when & then
			assertThatThrownBy(() ->
				Course.of(
					"강의 제목",
					"강의 설명",
					-1,
					new Capacity(25),
					LocalDate.of(2026, 8, 1),
					LocalDate.of(2026, 8, 31),
					creator
				)
			).isInstanceOf(BadRequestException.class);
		}

		@Test
		void 수강_시작일이_null이면_예외를_반환한다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();

			//when & then
			assertThatThrownBy(() ->
				Course.of(
					"강의 제목",
					"강의 설명",
					100_000,
					new Capacity(25),
					null,
					LocalDate.of(2026, 8, 31),
					creator
				)
			).isInstanceOf(BadRequestException.class);
		}

		@Test
		void 수강_종료일이_null이면_예외를_반환한다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();

			//when & then
			assertThatThrownBy(() ->
				Course.of(
					"강의 제목",
					"강의 설명",
					100_000,
					new Capacity(25),
					LocalDate.of(2026, 8, 1),
					null,
					creator
				)
			).isInstanceOf(BadRequestException.class);
		}

		@Test
		void 수강_시작일이_종료일보다_늦으면_예외를_반환한다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();

			//when & then
			assertThatThrownBy(() ->
				Course.of(
					"강의 제목",
					"강의 설명",
					100_000,
					new Capacity(25),
					LocalDate.of(2026, 9, 1),
					LocalDate.of(2026, 8, 31),
					creator
				)
			).isInstanceOf(BadRequestException.class);
		}
	}

	@Nested
	@DisplayName("강의 모집 시작")
	class OpenCourse {
		@Test
		void 초안_상태의_강의는_모집을_시작할_수_있다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);

			//when
			course.open();

			//then
			assertThat(course.isOpen()).isTrue();
		}

		@Test
		void 초안_상태가_아니면_모집을_시작할_수_없다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);
			course.open();

			//when & then
			assertThatThrownBy(course::open)
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("강의 모집 마감")
	class CloseCourse {
		@Test
		void 모집_중인_강의는_마감할_수_있다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);
			course.open();

			//when
			course.close();

			//then
			assertThat(course.isClosed()).isTrue();
		}

		@Test
		void 모집_중이_아니면_마감할_수_없다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);

			//when & then
			assertThatThrownBy(course::close)
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("수강 신청 가능 여부")
	class EnrollmentAvailability {
		@Test
		void 모집_중이고_정원이_남아있으면_신청_인원을_증가시킨다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);
			course.open();

			//when
			course.increaseEnrollmentCount();

			//then
			assertThat(course.getCapacity().getCurrent()).isEqualTo(1);
		}

		@Test
		void 모집_중이_아니면_신청할_수_없다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);

			//when & then
			assertThatThrownBy(course::validateCanEnroll)
				.isInstanceOf(ConflictException.class);
		}

		@Test
		void 모집_중이라면_정원이_가득_차도_수강_신청_검증은_통과한다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = Course.of(
				"강의 제목",
				"강의 설명",
				100_000,
				new Capacity(1),
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 31),
				creator
			);
			course.open();
			course.increaseEnrollmentCount();

			//when & then
			assertThat(course.isFull()).isTrue();
			assertThatCode(course::validateCanEnroll)
				.doesNotThrowAnyException();
		}

		@Test
		void 정원이_가득_찼다면_신청_인원을_증가시킬_수_없다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = Course.of(
				"강의 제목",
				"강의 설명",
				100_000,
				new Capacity(1),
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 31),
				creator
			);
			course.open();
			course.increaseEnrollmentCount();

			//when & then
			assertThatThrownBy(course::increaseEnrollmentCount)
				.isInstanceOf(ConflictException.class);
		}

		@Test
		void 현재_신청_인원이_있다면_감소시킬_수_있다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);
			course.open();
			course.increaseEnrollmentCount();

			//when
			course.decreaseEnrollmentCount();

			//then
			assertThat(course.getCapacity().getCurrent()).isZero();
		}

		@Test
		void 현재_신청_인원이_없다면_감소시킬_수_없다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);

			//when & then
			assertThatThrownBy(course::decreaseEnrollmentCount)
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("강의 공개 여부")
	class PublicationVisibility {
		@Test
		void 초안_상태의_강의는_공개되지_않는다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);

			//when & then
			assertThatThrownBy(course::validatePublished)
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 모집중인_강의는_공개된다() {
			//given
			User creator = UserFixture.USER_FIXTURE_1.createCreator();
			Course course = CourseFixture.COURSE_FIXTURE_1.create(creator);
			course.open();

			//when & then
			assertThatCode(course::validatePublished)
				.doesNotThrowAnyException();
		}
	}
}
