package com.example.academy.common.presentation.advice;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.academy.common.exception.CustomException;
import com.example.academy.common.presentation.dto.ApiErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionAdvice {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiErrorResponse<Void>> handleCustomException(CustomException e) {
        if (e.getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            log.error(e.getMessage());
        }
		return ResponseEntity.status(e.getStatus()).body(ApiErrorResponse.of(e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
		return ResponseEntity.status(BAD_REQUEST).body(
                ApiErrorResponse.of(e.getBindingResult().getFieldErrors())
        );
	}
}
