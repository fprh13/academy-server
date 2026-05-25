package com.example.academy.course.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Capacity {
	@Column(name = "max_capacity", nullable = false)
	private int max;

	@Column(name = "current_enrollment_count", nullable = false)
	private int current;

	public Capacity(final int max) {
		this.max = max;
		this.current = 0;
	}
}
