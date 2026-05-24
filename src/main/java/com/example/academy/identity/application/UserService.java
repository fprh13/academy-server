package com.example.academy.identity.application;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.academy.common.exception.CustomException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.identity.presentation.dto.request.user.RegisterUserRequest;
import com.example.academy.identity.presentation.dto.response.user.PublicUserProfileResponse;
import com.example.academy.identity.presentation.dto.response.user.UserProfileResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
	private static final String LOGIN_ID_DUPLICATE_MESSAGE = "이미 사용중인 아이디입니다.";
	private static final String EMAIL_DUPLICATE_MESSAGE = "이미 사용중인 이메일입니다.";
	private static final String DUPLICATE_MESSAGE = "아이디 혹은 이메일이 이미 사용중입니다.";

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	@Transactional
	public Long register(RegisterUserRequest registerUserRequest) {
		validateDuplicateLoginId(registerUserRequest.loginId());
		validateDuplicateEmail(registerUserRequest.email());

		String encodedPassword = bCryptPasswordEncoder.encode(registerUserRequest.password());
		try {
			return userRepository.save(registerUserRequest.toEntity(encodedPassword)).getId();
		} catch (DataIntegrityViolationException e) {
			throw new CustomException(HttpStatus.CONFLICT, DUPLICATE_MESSAGE);
		}
	}

	private void validateDuplicateLoginId(String loginId) {
		if (userRepository.existsByLoginId(loginId)) {
			throw new CustomException(HttpStatus.CONFLICT, LOGIN_ID_DUPLICATE_MESSAGE);
		}
	}

	private void validateDuplicateEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new CustomException(HttpStatus.CONFLICT, EMAIL_DUPLICATE_MESSAGE);
		}
	}

	public void checkDuplicateLoginId(String loginId) {
		validateDuplicateLoginId(loginId);
	}

	public void checkDuplicateEmail(String email) {
		validateDuplicateEmail(email);
	}

	public UserProfileResponse getProfileInfo(User user) {
		return UserProfileResponse.from(user);
	}

	public PublicUserProfileResponse getPublicProfileInfo(Long userId) {
		return userRepository.findById(userId)
			.map(PublicUserProfileResponse::from)
			.orElseThrow(() -> new NotFoundException(User.class));
	}
}
