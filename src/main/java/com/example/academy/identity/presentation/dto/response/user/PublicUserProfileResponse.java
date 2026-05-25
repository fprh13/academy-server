package com.example.academy.identity.presentation.dto.response.user;

import com.example.academy.identity.domain.user.Role;
import com.example.academy.identity.domain.user.User;

public record PublicUserProfileResponse(
	String email,
	String name,
	String role
) {
	public static PublicUserProfileResponse from(User user) {
		return new PublicUserProfileResponse(
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
