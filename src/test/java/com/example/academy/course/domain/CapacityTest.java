package com.example.academy.course.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.academy.common.exception.BadRequestException;
import com.example.academy.common.exception.ConflictException;

class CapacityTest {

	@Nested
	@DisplayName("정원 생성")
	class CreateCapacity {
		@Test
		void 최대_정원이_1명_미만이면_예외를_반환한다() {
			//when & then
			assertThatThrownBy(() -> new Capacity(0))
				.isInstanceOf(BadRequestException.class);
		}
	}

	@Nested
	@DisplayName("신청 인원 증가")
	class IncreaseCapacity {
		@Test
		void 정원이_남아있으면_신청_인원을_증가시킨다() {
			//given
			Capacity capacity = new Capacity(2);

			//when
			capacity.increase();

			//then
			assertThat(capacity.getCurrent()).isEqualTo(1);
		}

		@Test
		void 정원이_가득_차면_신청_인원을_증가시킬_수_없다() {
			//given
			Capacity capacity = new Capacity(1);
			capacity.increase();

			//when & then
			assertThatThrownBy(capacity::increase)
				.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("신청 인원 감소")
	class DecreaseCapacity {
		@Test
		void 현재_신청_인원이_있으면_감소시킨다() {
			//given
			Capacity capacity = new Capacity(2);
			capacity.increase();

			//when
			capacity.decrease();

			//then
			assertThat(capacity.getCurrent()).isZero();
		}

		@Test
		void 현재_신청_인원이_0명이면_감소시킬_수_없다() {
			//given
			Capacity capacity = new Capacity(2);

			//when & then
			assertThatThrownBy(capacity::decrease)
				.isInstanceOf(ConflictException.class);
		}
	}
}
