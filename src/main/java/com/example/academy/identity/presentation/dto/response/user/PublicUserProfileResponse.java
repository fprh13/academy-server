package com.example.academy.identity.presentation.dto.response.user;

import com.example.academy.identity.domain.user.User;

public record PublicUserProfileResponse(
	String email,
	String name
) {
	public static PublicUserProfileResponse from(User user) {
		return new PublicUserProfileResponse(user.getEmail(), user.getName());
	}
}
