package com.example.academy.enrollment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnrollmentCancelPolicyTest {

	@Test
	@DisplayName("결제 후 7일째까지는 취소할 수 있다")
	void 결제_후_7일째까지는_취소할_수_있다() {
		// given
		LocalDateTime paidAt = LocalDateTime.of(2026, 6, 1, 10, 0);

		// when
		boolean result = EnrollmentCancelPolicy.canCancel(paidAt, paidAt.plusDays(7));

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("결제 후 7일이 지나면 취소할 수 없다")
	void 결제_후_7일이_지나면_취소할_수_없다() {
		// given
		LocalDateTime paidAt = LocalDateTime.of(2026, 6, 1, 10, 0);

		// when
		boolean result = EnrollmentCancelPolicy.canCancel(paidAt, paidAt.plusDays(7).plusSeconds(1));

		// then
		assertThat(result).isFalse();
	}
}
