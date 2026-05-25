package com.example.academy.support.fixture;

import java.time.LocalDate;

import com.example.academy.course.domain.Capacity;
import com.example.academy.course.domain.Course;
import com.example.academy.identity.domain.user.User;

public enum CourseFixture {
	COURSE_FIXTURE_1(
		"자바 입문",
		"자바 기초 문법을 학습하는 강의입니다.",
		100000,
		30,
		LocalDate.of(2026, 6, 1),
		LocalDate.of(2026, 6, 30)
	),
	COURSE_FIXTURE_2(
		"스프링 부트 실전",
		"스프링 부트로 웹 애플리케이션을 구현하는 강의입니다.",
		150000,
		20,
		LocalDate.of(2026, 7, 1),
		LocalDate.of(2026, 7, 31)
	),
	COURSE_FIXTURE_3(
		"JPA 마스터 클래스",
		"JPA 매핑과 성능 최적화를 다루는 강의입니다.",
		200000,
		25,
		LocalDate.of(2026, 8, 1),
		LocalDate.of(2026, 8, 31)
	);

	private final String title;
	private final String description;
	private final int price;
	private final int maxCapacity;
	private final LocalDate startDate;
	private final LocalDate endDate;

	CourseFixture(String title, String description, int price, int maxCapacity, LocalDate startDate, LocalDate endDate) {
		this.title = title;
		this.description = description;
		this.price = price;
		this.maxCapacity = maxCapacity;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Course create(User creator) {
		return Course.of(
			title,
			description,
			price,
			new Capacity(maxCapacity),
			startDate,
			endDate,
			creator
		);
	}
}
