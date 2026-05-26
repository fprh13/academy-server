package com.example.academy.enrollment.domain;

import java.time.LocalDateTime;

import com.example.academy.common.domain.AggregateRoot;
import com.example.academy.common.exception.BadRequestException;
import com.example.academy.common.exception.ConflictException;
import com.example.academy.course.domain.Course;
import com.example.academy.identity.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "enrollments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends AggregateRoot<Enrollment> {

	@Enumerated(EnumType.STRING)
	@Column(name = "state", length = 20, nullable = false)
	private EnrollmentState state;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private Enrollment(Course course, User user) {
		course.increaseEnrollmentCount();
		this.state = EnrollmentState.PENDING;
		this.course = course;
		this.user = user;
	}

	public static Enrollment apply(Course course, User user) {
		return new Enrollment(course, user);
	}

	public void cancelApplication(LocalDateTime now) {
		if (state != EnrollmentState.PENDING) {
			throw new ConflictException("결제 대기 상태의 수강 신청만 취소할 수 있습니다.");
		}
		this.course.decreaseEnrollmentCount();
	}

	public void confirmPayment(LocalDateTime paidAt) {
		if (state != EnrollmentState.PENDING) {
			throw new ConflictException("결제 대기 상태의 수강 신청만 결제 확정할 수 있습니다.");
		}
		this.state = EnrollmentState.CONFIRMED;
		this.paidAt = paidAt;
	}

	public void cancelConfirmed(LocalDateTime now) {
		if (state != EnrollmentState.CONFIRMED) {
			throw new ConflictException("결제 확정 상태의 수강 신청만 취소할 수 있습니다.");
		}
		if (!EnrollmentCancelPolicy.canCancel(paidAt, now)) {
			throw new BadRequestException("결제 후 7일이 지나 취소할 수 없습니다.");
		}
		this.state = EnrollmentState.CANCELLED;
		this.cancelledAt = now;
		this.course.decreaseEnrollmentCount();
	}

	public boolean isPending() {
		return state == EnrollmentState.PENDING;
	}

	public boolean isConfirmed() {
		return state == EnrollmentState.CONFIRMED;
	}

	public boolean isCancelled() {
		return state == EnrollmentState.CANCELLED;
	}
}
