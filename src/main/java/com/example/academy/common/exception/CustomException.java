package com.example.academy.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "서버에서 오류가 발생했습니다.";

    private final HttpStatus status;
    private final String message;

    public CustomException() {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = DEFAULT_MESSAGE;
    }

    public CustomException(HttpStatus status) {
        this.status = status;
        this.message = DEFAULT_MESSAGE;
    }

    public CustomException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
