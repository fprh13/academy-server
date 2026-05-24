package com.example.academy.identity.application;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.academy.common.exception.CustomException;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.identity.infrastructure.jwt.JwtTokenProvider;
import com.example.academy.identity.presentation.dto.request.auth.AuthenticateUserRequest;
import com.example.academy.identity.presentation.dto.response.auth.AuthenticateUserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final static String MATCH_ERROR_MESSAGE = "아이디 혹은 비밀번호가 일치하지 않습니다.";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticateUserResponse authenticate(AuthenticateUserRequest authenticateUserRequest) {

        User user = userRepository.findByLoginId(authenticateUserRequest.loginId())
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, MATCH_ERROR_MESSAGE));

		if (!bCryptPasswordEncoder.matches(authenticateUserRequest.password(), user.getPassword())) {
			throw new CustomException(HttpStatus.BAD_REQUEST, MATCH_ERROR_MESSAGE);
		}
        Date now = new Date();
		String accessToken = jwtTokenProvider.createAccessToken(user, now);

        return new AuthenticateUserResponse(accessToken);
    }
}
