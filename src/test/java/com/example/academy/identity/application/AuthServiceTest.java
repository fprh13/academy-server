package com.example.academy.identity.application;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.academy.common.exception.CustomException;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.identity.infrastructure.jwt.JwtTokenProvider;
import com.example.academy.identity.presentation.dto.request.auth.AuthenticateUserRequest;
import com.example.academy.support.fixture.UserFixture;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	AuthService authService;

	@Mock
	UserRepository userRepository;

	@Mock
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Mock
	JwtTokenProvider jwtTokenProvider;

	@Nested
	@DisplayName("인증(로그인) 기능")
	class Authenticate {
		@Test
		void 아이디로_사용자를_조회한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUserRequest authenticateUserRequest = new AuthenticateUserRequest(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findByLoginId(authenticateUserRequest.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUserRequest.password(), user.getPassword()))
				.thenReturn(true);

			//when
			authService.authenticate(authenticateUserRequest);

			//then
			Mockito.verify(userRepository, Mockito.times(1))
				.findByLoginId(authenticateUserRequest.loginId());
		}

		@Test
		void 아이디에_해당하는_사용자가_없다면_예외를_반환한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUserRequest authenticateUserRequest = new AuthenticateUserRequest(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findByLoginId(authenticateUserRequest.loginId()))
				.thenThrow(CustomException.class);

		    //when & then
			assertThatThrownBy(() -> authService.authenticate(authenticateUserRequest))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 비밀번호를_검증한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUserRequest authenticateUserRequest = new AuthenticateUserRequest(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findByLoginId(authenticateUserRequest.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUserRequest.password(), user.getPassword()))
				.thenReturn(true);

		    //when
			authService.authenticate(authenticateUserRequest);

		    //then
		    Mockito.verify(bCryptPasswordEncoder, Mockito.times(1))
				.matches(authenticateUserRequest.password(), user.getPassword());
		}

		@Test
		void 비밀번호_검증에_실패한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUserRequest authenticateUserRequest = new AuthenticateUserRequest(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findByLoginId(authenticateUserRequest.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUserRequest.password(), user.getPassword()))
				.thenReturn(false);

		    //when & then
			assertThatThrownBy(() -> authService.authenticate(authenticateUserRequest))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 응답할_엑세스_토큰을_생성한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUserRequest authenticateUserRequest = new AuthenticateUserRequest(
				user.getLoginId(),
				user.getPassword()
			);
			Mockito.when(userRepository.findByLoginId(authenticateUserRequest.loginId()))
				.thenReturn(Optional.of(user));
			Mockito.when(bCryptPasswordEncoder.matches(authenticateUserRequest.password(), user.getPassword()))
				.thenReturn(true);
			Mockito.when(jwtTokenProvider.createAccessToken(Mockito.any(User.class), Mockito.any()))
				.thenReturn("accessToken");

		    //when
			authService.authenticate(authenticateUserRequest);

		    //then
			Mockito.verify(jwtTokenProvider, Mockito.times(1))
				.createAccessToken(Mockito.any(), Mockito.any());
		}
	}
}