package com.example.academy.common.presentation.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private static final String DEFAULT_SUCCESS_MESSAGE = "OK";

	private final String message;
	private final T data;

	@Builder(access = AccessLevel.PRIVATE)
	private ApiResponse(String message, T data) {
		this.message = message;
		this.data = data;
	}

    public static <T> ApiResponse<T> of() {
        return ApiResponse.<T>builder()
                .message(DEFAULT_SUCCESS_MESSAGE)
                .data(null)
                .build();
    }

	public static <T> ApiResponse<T> of(T data) {
		return ApiResponse.<T>builder()
			.message(DEFAULT_SUCCESS_MESSAGE)
			.data(data)
			.build();
	}

	public static <T> ApiResponse<T> of(String message, T data) {
		return ApiResponse.<T>builder()
			.message(message)
			.data(data)
			.build();
	}
}
