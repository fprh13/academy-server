package com.example.academy.identity.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.academy.common.exception.CustomException;
import com.example.academy.identity.application.AuthService;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.identity.infrastructure.jwt.JwtTokenProvider;
import com.example.academy.identity.presentation.dto.request.auth.AuthenticateUserRequest;
import com.example.academy.identity.presentation.dto.response.auth.AuthenticateUserResponse;
import com.example.academy.support.IntegrationSupportTest;
import com.example.academy.support.fixture.UserFixture;

class AuthServiceIntegrationTest extends IntegrationSupportTest {

	@Autowired
	AuthService authService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        User userFixture = UserFixture.USER_FIXTURE_1.create();

        user = userRepository.save(
                User.register(
                        userFixture.getLoginId(),
                        bCryptPasswordEncoder.encode(userFixture.getPassword()),
                        userFixture.getEmail(),
                        userFixture.getName()
                )
        );
    }

	@Nested
	@DisplayName("인증(로그인) 기능")
	class Authenticate {
		@Test
		void 인증을_한다() {
			//given
			User requestUser = UserFixture.USER_FIXTURE_1.create();

			AuthenticateUserRequest authenticateUserRequest = new AuthenticateUserRequest(
				requestUser.getLoginId(),
				requestUser.getPassword()
			);

			//when
			AuthenticateUserResponse authenticateUserResponse = authService.authenticate(authenticateUserRequest);

			//then
			assertAll(
				() -> assertThat(authenticateUserResponse.accessToken()).isNotNull()
			);
		}

		@Test
		void 아이디가_존재하지_않는다면_예외를_반환한다() {
		    //given
			User requestUser = UserFixture.USER_FIXTURE_1.create();

			AuthenticateUserRequest authenticateUserRequest = new AuthenticateUserRequest(
				"nonExistentId",
				requestUser.getPassword()
			);

		    //when & then
			assertThatThrownBy(() -> authService.authenticate(authenticateUserRequest))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 비밀번호가_올바르지_않다면_예외를_반환한다() {
		    //given
			User requestUser = UserFixture.USER_FIXTURE_1.create();

			AuthenticateUserRequest authenticateUserRequest = new AuthenticateUserRequest(
				requestUser.getLoginId(),
				"wrongPassword1234@"
			);

		    //when & then
			assertThatThrownBy(() -> authService.authenticate(authenticateUserRequest))
				.isInstanceOf(CustomException.class);
		}
	}
}
