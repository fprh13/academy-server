package com.example.academy.identity.presentation.dto.response.user;

import com.example.academy.identity.domain.user.Role;
import com.example.academy.identity.domain.user.User;

public record UserProfileResponse(
	String loginId,
	String email,
	String name,
	String role
) {
	public static UserProfileResponse from(User user) {
		return new UserProfileResponse(
			user.getLoginId(),
			user.getEmail(),
			user.getName(),
			resolveRole(user.getRole())
		);
	}

	private static String resolveRole(Role role) {
		if (role == Role.CREATOR) {
			return "CREATOR";
		}
		return "USER";
	}
}
