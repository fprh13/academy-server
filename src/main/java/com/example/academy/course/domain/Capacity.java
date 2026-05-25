package com.example.academy.course.domain;

import com.example.academy.common.exception.BadRequestException;
import com.example.academy.common.exception.ConflictException;

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

	public Capacity(int max) {
		validateMax(max);

		this.max = max;
		this.current = 0;
	}

	public void increase() {
		if (isFull()) {
			throw new ConflictException("정원이 가득 찼습니다.");
		}

		this.current++;
	}

	public void decrease() {
		if (current <= 0) {
			throw new ConflictException("현재 신청 인원이 0명입니다.");
		}

		this.current--;
	}

	public boolean isFull() {
		return current >= max;
	}

	private void validateMax(final int max) {
		if (max <= 0) {
			throw new BadRequestException("최대 정원은 1명 이상이어야 합니다.");
		}
	}
}
