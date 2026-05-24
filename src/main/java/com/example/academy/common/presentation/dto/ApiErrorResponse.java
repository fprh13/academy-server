package com.example.academy.common.presentation.dto;

import java.util.List;

import org.springframework.validation.FieldError;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ApiErrorResponse<T> {
    private static final String FIELD_ERROR_MESSAGE = "의 필드 값 유효하지 않습니다.";

    private final String message;
    private final T data;

    @Builder(access = AccessLevel.PRIVATE)
    private ApiErrorResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public static ApiErrorResponse<Void> of(String message) {
        return ApiErrorResponse.<Void>builder()
                .message(message)
                .data(null)
                .build();
    }

    public static ApiErrorResponse<Void> of(List<FieldError> fieldErrors) {
        FieldError fieldError = fieldErrors
                .get(fieldErrors.size() - 1);
        return ApiErrorResponse.<Void>builder()
                .message(fieldError.getField() + FIELD_ERROR_MESSAGE)
                .data(null)
                .build();
    }
}
