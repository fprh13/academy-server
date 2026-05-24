package com.example.academy.identity.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import com.example.academy.common.exception.CustomException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.common.presentation.dto.ApiErrorResponse;
import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.presentation.dto.request.user.RegisterUserRequest;
import com.example.academy.identity.presentation.dto.response.user.PublicUserProfileResponse;
import com.example.academy.identity.presentation.dto.response.user.UserProfileResponse;
import com.example.academy.support.RestDocsSupport;
import com.example.academy.support.fixture.UserFixture;

class UserControllerTest extends RestDocsSupport {

	private static final String BASE_URI = "/users";
	private static final String BASE_TAG = "Identity - User";

	@Nested
	@DisplayName("회원가입 API 테스트")
	class RegisterTest {
		@Test
		void 회원가입_2XX() throws Exception {
			//given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			Mockito.when(userService.register(any(RegisterUserRequest.class)))
				.thenReturn(any(Long.class));
			RegisterUserRequest requestDto = new RegisterUserRequest(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions

				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isNotEmpty())
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("회원 가입")
						.description("## 회원 가입 기능 \n"
							+ "### 사용법 \n"
							+ "- 필드의 validation을 확인해주세요.\n"
							+ "- 아이디와 이메일 중복 체크 완료 후 진행해주세요."
						)
						.requestSchema(Schema.schema(RegisterUserRequest.class.getSimpleName()))
						.requestFields(
							fieldWithPath("loginId").description("아이디는 영문 4자리 이상입니다.").type(JsonFieldType.STRING),
							fieldWithPath("password").description("비밀번호는 특수문자를 포함한 영문과 숫자 8자리 이상입니다.").type(JsonFieldType.STRING),
							fieldWithPath("email").description("이메일 형식을 지켜주세요.").type(JsonFieldType.STRING),
							fieldWithPath("name").description("사용자 이름입니다.").type(JsonFieldType.STRING)
						)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 회원가입_4XX_아이디_중복() throws Exception {
			//given
			String errorMessage = "이미 사용 중인 아이디입니다.";

			Mockito.doThrow(new CustomException(HttpStatus.CONFLICT, errorMessage))
				.when(userService)
				.register(any(RegisterUserRequest.class));

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUserRequest requestDto = new RegisterUserRequest(
				"duplicateId",
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isConflict())
				.andExpect(result -> Assertions.assertInstanceOf(CustomException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.requestSchema(Schema.schema(RegisterUserRequest.class.getSimpleName()))
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
					)
				);
		}

		@Test
		void 회원가입_4XX_이메일_중복() throws Exception {
			//given
			String errorMessage = "이미 사용 중인 이메일입니다.";

			Mockito.doThrow(new CustomException(HttpStatus.CONFLICT, errorMessage))
				.when(userService)
				.register(any(RegisterUserRequest.class));

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUserRequest requestDto = new RegisterUserRequest(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				"duplicate@test.com",
				userFixture.getName()
			);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isConflict())
				.andExpect(result -> Assertions.assertInstanceOf(CustomException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.requestSchema(Schema.schema(RegisterUserRequest.class.getSimpleName()))
							.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
							.build())
					)
				);
		}

		@Test
		void 회원가입_4XX_요청_데이터_유효성_검사_실패() throws Exception {
			//given
			String errorMessage = "password" + BASE_FIELD_ERROR_MESSAGE;

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUserRequest requestDto = new RegisterUserRequest(
				userFixture.getLoginId(),
				"1234",
				userFixture.getEmail(),
				userFixture.getName()
			);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			Mockito.verify(userService, Mockito.never()).register(any());
			actions
				.andExpect(status().isBadRequest())
				.andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.requestSchema(Schema.schema(RegisterUserRequest.class.getSimpleName()))
							.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
							.build())
					)
				);
		}
	}

	@Nested
	@DisplayName("아이디 중복 체크 API 테스트")
	class CheckDuplicateLoginId {
		@Test
		void 아이디_중복_체크_2XX() throws Exception {
		    //given
			String loginId = "testLoginId";
			Mockito.doNothing().when(userService).checkDuplicateLoginId(loginId);

		    //when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/login-id/exists")
					.queryParam("loginId", loginId)
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("아이디 중복 체크")
						.description("## 아이디 중복 체크 기능 \n"
							+ "### 사용법 \n"
							+ "- 아이디를 쿼리 파라미터로 전송합니다.\n"
							+ "- 200응답이라면 사용 가능합니다."
						)
						.queryParameters(
							parameterWithName("loginId").description("검증 대상 아이디").type(SimpleType.STRING)
						)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.build())
					)
				);
		}

		@Test
		void 아이디_중복_체크_4XX_loingId_중복() throws Exception {
			//given
			String loginId = "test1";

			String errorMessage = "이미 사용 중인 아이디입니다.";
			Mockito.doThrow(new CustomException(HttpStatus.CONFLICT, errorMessage))
				.when(userService)
				.checkDuplicateLoginId(loginId);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/login-id/exists")
					.queryParam("loginId", loginId)
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isConflict())
				.andExpect(result -> Assertions.assertInstanceOf(CustomException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
							.build())
					)
				);
		}
	}

	@Nested
	@DisplayName("이메일 중복 체크 API 테스트")
	class CheckDuplicateEmail {
		@Test
		void 이메일_중복_체크_2XX() throws Exception {
			//given
			String email = "test1@test.com";
			Mockito.doNothing().when(userService).checkDuplicateEmail(email);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/email/exists")
					.queryParam("email", email)
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("이메일 중복 체크")
							.description("## 이메일 중복 체크 기능 \n"
								+ "### 사용법 \n"
								+ "- 이메일을 쿼리 파라미터로 전송합니다.\n"
								+ "- 200응답이라면 사용 가능합니다."
							)
							.queryParameters(
								parameterWithName("email").description("검증 대상 이메일").type(SimpleType.STRING)
							)
							.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
							.build())
					)
				);
		}

		@Test
		void 이메일_중복_체크_4XX_email_중복() throws Exception {
			//given
			String email = "test@test.com";

			String errorMessage = "이미 사용 중인 이메일입니다.";
			Mockito.doThrow(new CustomException(HttpStatus.CONFLICT, errorMessage))
				.when(userService)
				.checkDuplicateEmail(email);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/email/exists")
					.queryParam("email", email)
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isConflict())
				.andExpect(result -> Assertions.assertInstanceOf(CustomException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
							.build())
					)
				);
		}
	}

	@Nested
	@DisplayName("프로필 조회 API 테스트")
	class GetUserProfileResponse {
		@Test
		void 프로필_조회_2XX() throws Exception {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			UserProfileResponse userProfileResponse = UserProfileResponse.from(userFixture);
			Mockito.when(userService.getProfileInfo(any(User.class))).thenReturn(userProfileResponse);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/profile")
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data.loginId").value(userProfileResponse.loginId()))
				.andExpect(jsonPath("$.data.email").value(userProfileResponse.email()))
				.andExpect(jsonPath("$.data.name").value(userProfileResponse.name()))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("프로필 조회")
							.responseSchema(Schema.schema(UserProfileResponse.class.getSimpleName()))
							.responseFields(
								fieldWithPath("message").description("성공 응답 메세지입니다.").type(JsonFieldType.STRING),
								fieldWithPath("data.loginId").description("사용자 아이디입니다.").type(JsonFieldType.STRING),
								fieldWithPath("data.email").description("사용자 이메일입니다.").type(JsonFieldType.STRING),
								fieldWithPath("data.name").description("사용자 이름입니다.").type(JsonFieldType.STRING)
							)
							.build())
					)
				);
		}
	}

	@Nested
	@DisplayName("공개 프로필 조회 API 테스트")
	class GetPublicUserProfileResponse {
		@Test
		void 공개_프로필_조회_2XX() throws Exception {
		    //given
			Long userId = 1L;
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			PublicUserProfileResponse publicUserProfileResponse = PublicUserProfileResponse.from(userFixture);
			Mockito.when(userService.getPublicProfileInfo(userId)).thenReturn(publicUserProfileResponse);

		    //when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/{userId}", userId)
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data.email").value(publicUserProfileResponse.email()))
				.andExpect(jsonPath("$.data.name").value(publicUserProfileResponse.name()))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("공개 프로필 조회")
							.pathParameters(
								parameterWithName("userId").description("조회할 사용자의 PK입니다.")
							)
							.responseSchema(Schema.schema(PublicUserProfileResponse.class.getSimpleName()))
							.responseFields(
								fieldWithPath("message").description("성공 응답 메세지입니다.").type(JsonFieldType.STRING),
								fieldWithPath("data.email").description("사용자 이메일입니다.").type(JsonFieldType.STRING),
								fieldWithPath("data.name").description("사용자 이름입니다.").type(JsonFieldType.STRING)
							)
							.build()
						)
					)
				);
		}

		@Test
		void 공개_프로필_조회_4XX_NOTFOUND() throws Exception {
		    //given
			String errorMessage = User.class.getSimpleName() + "을(를) 찾을 수 없습니다.";

			Long userId = 1L;
			Mockito.doThrow(new NotFoundException(User.class))
				.when(userService)
				.getPublicProfileInfo(userId);

		    //when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/{userId}", userId)
					.contentType(MediaType.APPLICATION_JSON));

		    //then
			actions
				.andExpect(status().isNotFound())
				.andExpect(
					result -> Assertions.assertInstanceOf(NotFoundException.class, result.getResolvedException())
				)
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
							.build())
					)
				);
		}
	}
}