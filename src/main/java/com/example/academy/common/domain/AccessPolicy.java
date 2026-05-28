package com.example.academy.common.domain;

public interface AccessPolicy {
	boolean canAccess(Long userId);
}
