package com.example.academy.course.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.identity.domain.user.User;
import com.example.academy.support.fixture.CourseFixture;
import com.example.academy.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
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
			Course result = Course.of(course.getTitle(), course.getDescription(), course.getPrice(),
				course.getCapacity(), course.getStartDate(), course.getEndDate(), creator);

			//then
			Assertions.assertThat(result.getState()).isEqualTo(CourseState.DRAFT);
		}
		
		@Test
		void 강사가_아니라면_예외를_반환한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
		    
		    //when & then
			Assertions.assertThatThrownBy(() ->
					Course.of(
						"강사되는 법 마스터",
						"강사가 아니지만, 강의를 올려보는 실습을 해봅니다.",
						200_000,
						new Capacity(25),
						LocalDate.of(2026, 8, 1),
						LocalDate.of(2026, 8, 31),
						user
					)
				)
				.isInstanceOf(ForbiddenException.class);
		}
	}

}