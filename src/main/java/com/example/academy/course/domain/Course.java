package com.example.academy.course.domain;

import static jakarta.persistence.FetchType.*;

import java.time.LocalDate;

import com.example.academy.common.domain.AccessPolicy;
import com.example.academy.common.domain.AggregateRoot;
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

	@Override
	public boolean canRead(Long userId) {
		return true;
	}

	@Override
	public boolean canWrite(Long userId) {
		return creator.getId().equals(userId);
	}
}
