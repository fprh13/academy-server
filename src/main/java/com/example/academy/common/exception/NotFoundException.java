package com.example.academy.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends CustomException {
	private static final String DEFAULT_NOTFOUND_ERROR_MESSAGE = "을(를) 찾을 수 없습니다.";

	public NotFoundException(Class<?> entityType) {
		super(HttpStatus.NOT_FOUND, entityType.getSimpleName() + DEFAULT_NOTFOUND_ERROR_MESSAGE);
	}
}
