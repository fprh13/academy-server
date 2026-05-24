package com.example.academy.identity.presentation.dto.response.user;

import com.example.academy.identity.domain.user.User;

public record UserProfileResponse(
	String loginId,
	String email,
	String name
) {
	public static UserProfileResponse from(User user) {
		return new UserProfileResponse(user.getLoginId(), user.getEmail(), user.getName());
	}
}
