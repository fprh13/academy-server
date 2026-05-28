package com.example.academy.identity.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.academy.common.exception.CustomException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.identity.application.UserService;
import com.example.academy.identity.domain.user.Role;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.identity.presentation.dto.request.user.RegisterUserRequest;
import com.example.academy.identity.presentation.dto.response.user.PublicUserProfileResponse;
import com.example.academy.identity.presentation.dto.response.user.UserProfileResponse;
import com.example.academy.support.IntegrationSupportTest;
import com.example.academy.support.fixture.UserFixture;

class UserServiceIntegrationTest extends IntegrationSupportTest {

	private static final String USER_NOT_SAVED_MESSAGE = "회원이 저장되지 않았습니다.";

    @Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Nested
	@DisplayName("회원 가입 기능")
	class UserRegisterTest {
		@Test
		void 회원가입을_한다() {
			//given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUserRequest requestDto = new RegisterUserRequest(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName(),
				false
			);
			//when
			Long userId = userService.register(requestDto);

			//then
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new AssertionError(USER_NOT_SAVED_MESSAGE));

			assertAll(
				() -> assertThat(user.getLoginId()).isEqualTo(requestDto.loginId()),
				() -> assertThat(user.getEmail()).isEqualTo(requestDto.email()),
				() -> assertThat(user.getName()).isEqualTo(requestDto.name()),
				() -> assertThat(bCryptPasswordEncoder.matches(requestDto.password(), user.getPassword())).isTrue()
			);
		}

		@Test
		void 강사로_회원가입을_한다() {
			//given
			User userFixture = UserFixture.USER_FIXTURE_2.createCreator();
			RegisterUserRequest requestDto = new RegisterUserRequest(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName(),
				true
			);
			//when
			Long userId = userService.register(requestDto);

			//then
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new AssertionError(USER_NOT_SAVED_MESSAGE));

			assertAll(
				() -> assertThat(user.getLoginId()).isEqualTo(requestDto.loginId()),
				() -> assertThat(user.getEmail()).isEqualTo(requestDto.email()),
				() -> assertThat(user.getName()).isEqualTo(requestDto.name()),
				() -> assertThat(user.getRole().getKey()).isEqualTo(Role.CREATOR.getKey()),
				() -> assertThat(bCryptPasswordEncoder.matches(requestDto.password(), user.getPassword())).isTrue()
			);
		}

		@Test
		void 아이디가_중복이면_예외를_반환한다() {
		    //given
			String loginId = "testLoginId";

			RegisterUserRequest otherRequestDto = new RegisterUserRequest(
				loginId,
				"test1@1234",
				"test1@test.com",
				"홍길동",
				false
			);
			userService.register(otherRequestDto);

			RegisterUserRequest requestDto = new RegisterUserRequest(
				loginId,
				"test2@1234",
				"test2@test.com",
				"아이디중복유저",
				false
			);

			//when & then
			Assertions.assertThatThrownBy(() -> userService.register(requestDto))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 이메일이_중복이면_예외를_반환한다() {
			//given
			String email = "test@test.com";

			RegisterUserRequest otherRequestDto = new RegisterUserRequest(
				"testUser1",
				"test1@1234",
				email,
				"홍길동",
				false
			);
			userService.register(otherRequestDto);

			RegisterUserRequest requestDto = new RegisterUserRequest(
				"testUser2",
				"test2@1234",
				email,
				"이메일중복유저",
				false
			);

			//when & then
			Assertions.assertThatThrownBy(() -> userService.register(requestDto))
				.isInstanceOf(CustomException.class);
		}
	}

	@Nested
	@DisplayName("아이디 중복 체크 기능")
	class checkDuplicateLoginId {
		@Test
		void 아이디가_중복이면_예외를_반환한다() {
		    //given
			String loginId = "testLoginId";

			RegisterUserRequest otherRequestDto = new RegisterUserRequest(
				loginId,
				"test1@1234",
				"test1@test.com",
				"홍길동",
				false
			);
			userRepository.save(otherRequestDto.toEntity(bCryptPasswordEncoder.encode(otherRequestDto.password())));

		    //when & then
		    Assertions.assertThatThrownBy(() -> userService.checkDuplicateLoginId(loginId))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 아이디_중복을_확인한다() {
		    //given
			String loginId = "testLoginId";

		    //when & then
		    Assertions.assertThatNoException()
				.isThrownBy(() -> userService.checkDuplicateLoginId(loginId));
		}
	}

	@Nested
	@DisplayName("이메일 중복 체크 기능")
	class checkDuplicateEmail {
		@Test
		void 이메일이_중복이면_예외를_반환한다() {
		    //given
			String email = "test@test.com";

			RegisterUserRequest otherRequestDto = new RegisterUserRequest(
				"testUser1",
				"test1@1234",
				email,
				"홍길동",
				false
			);
			userRepository.save(otherRequestDto.toEntity(bCryptPasswordEncoder.encode(otherRequestDto.password())));

		    //when & then
			Assertions.assertThatThrownBy(() -> userService.checkDuplicateEmail(email))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 이메일_중복을_확인한다() {
		    //given
			String email = "test@test.com";

		    //when & then
			Assertions.assertThatNoException()
				.isThrownBy(() -> userService.checkDuplicateEmail(email));
		}
	}

	@Nested
	@DisplayName("프로필 조회 기능")
	class GetUserProfileResponse {
		@Test
		void 프로필을_응답한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			UserProfileResponse userProfileResponse = UserProfileResponse.from(user);

			//when
			UserProfileResponse result = userService.getProfileInfo(user);

			//then
			assertThat(result).isEqualTo(userProfileResponse);
		}
	}

	@Nested
	@DisplayName("공개 프로필 조회 기능")
	class GetPublicUserProfileResponse {
		@Test
		void 공개_프로필을_응답한다() {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();

			User user = userRepository.save(userFixture);
			PublicUserProfileResponse publicUserProfileResponse = PublicUserProfileResponse.from(user);

			//when
			PublicUserProfileResponse result = userService.getPublicProfileInfo(user.getId());

			//then
			Assertions.assertThat(result).isEqualTo(publicUserProfileResponse);

		}

		@Test
		void 회원을_찾지_못하면_예외를_반환한다() {
		    //given
			Long userId = 1L;

		    //when & then
			Assertions.assertThatThrownBy(() -> userService.getPublicProfileInfo(userId))
				.isInstanceOf(NotFoundException.class);
		}
	}
}