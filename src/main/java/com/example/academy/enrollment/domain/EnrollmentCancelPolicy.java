package com.example.academy.enrollment.domain;

import java.time.LocalDateTime;

public class EnrollmentCancelPolicy {
	private static final int CANCEL_AVAILABLE_DAYS = 7;

	public static boolean canCancel(LocalDateTime paidAt, LocalDateTime now) {
		return !now.isAfter(paidAt.plusDays(CANCEL_AVAILABLE_DAYS));
	}
}
