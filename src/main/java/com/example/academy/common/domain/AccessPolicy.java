package com.example.academy.common.domain;

public interface AccessPolicy {
	boolean canRead(Long userId);
	boolean canWrite(Long userId);
}
