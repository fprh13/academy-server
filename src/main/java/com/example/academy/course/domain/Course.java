package com.example.academy.course.domain;

import static com.example.academy.identity.domain.user.Role.*;
import static jakarta.persistence.FetchType.*;

import java.time.LocalDate;

import com.example.academy.common.domain.AccessPolicy;
import com.example.academy.common.domain.AggregateRoot;
import com.example.academy.common.exception.BadRequestException;
import com.example.academy.common.exception.ConflictException;
import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.identity.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends AggregateRoot<Course> implements AccessPolicy {

	@Column(name = "title", length = 100, nullable = false)
	private String title;

	@Column(name = "description", length = 1000, nullable = false)
	private String description;

	@Column(name = "price", nullable = false)
	private int price;

	@Embedded
	private Capacity capacity;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "state", length = 20, nullable = false)
	private CourseState state;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "creator_id", nullable = false)
	private User creator;

	private Course(String title, String description, int price, Capacity capacity, LocalDate startDate, LocalDate endDate, User creator) {
		validateCreatorRole(creator);
		validatePrice(price);
		validatePeriod(startDate, endDate);
		this.title = title;
		this.description = description;
		this.price = price;
		this.capacity = capacity;
		this.startDate = startDate;
		this.endDate = endDate;
		this.state = CourseState.DRAFT;
		this.creator = creator;
	}

	public static Course of(String title, String description, int price, Capacity capacity, LocalDate startDate, LocalDate endDate, User creator) {
		return new Course(title, description, price, capacity, startDate, endDate, creator);
	}

	public void open() {
		if (state != CourseState.DRAFT) {
			throw new ConflictException("초안 상태의 강의만 모집을 시작할 수 있습니다.");
		}
		this.state = CourseState.OPEN;
	}

	public void close() {
		if (state != CourseState.OPEN) {
			throw new ConflictException("모집 중인 강의만 마감할 수 있습니다.");
		}
		this.state = CourseState.CLOSED;
	}

	public void increaseEnrollmentCount() {
		capacity.increase();
	}

	public void decreaseEnrollmentCount() {
		capacity.decrease();
	}

	private static void validateCreatorRole(User creator) {
		if (creator.getRole() != CREATOR) {
			throw new ForbiddenException();
		}
	}

	private void validatePrice(int price) {
		if (price < 0) {
			throw new BadRequestException("강의 가격은 0원 이상이어야 합니다.");
		}
	}

	private void validatePeriod(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) {
			throw new BadRequestException("수강 기간은 필수입니다.");
		}

		if (startDate.isAfter(endDate)) {
			throw new BadRequestException("수강 시작일은 종료일보다 늦을 수 없습니다.");
		}
	}

	public void validateCanEnroll() {
		if (state != CourseState.OPEN) {
			throw new ConflictException("모집 중인 강의만 신청할 수 있습니다.");
		}
	}

	public void validatePublished() {
		if (state == CourseState.DRAFT) {
			throw new NotFoundException(Course.class);
		}
	}

	public boolean isOpen() {
		return state == CourseState.OPEN;
	}

	public boolean isClosed() {
		return state == CourseState.CLOSED;
	}

	public boolean isFull() {
		return capacity.isFull();
	}

	@Override
	public boolean canAccess(Long userId) {
		return creator.getId().equals(userId);
	}
}
