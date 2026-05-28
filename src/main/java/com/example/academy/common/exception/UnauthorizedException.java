package com.example.academy.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends CustomException{
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
