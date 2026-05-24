package com.example.academy.identity.presentation;

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
import com.example.academy.common.exception.CustomException;
import com.example.academy.common.presentation.dto.ApiErrorResponse;
import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.infrastructure.jwt.JwtConstants;
import com.example.academy.identity.presentation.dto.request.auth.AuthenticateUserRequest;
import com.example.academy.identity.presentation.dto.response.auth.AuthenticateUserResponse;
import com.example.academy.support.RestDocsSupport;
import com.example.academy.support.fixture.UserFixture;


class AuthControllerTest extends RestDocsSupport {

	private static final String BASE_URI = "/auth";
	private static final String BASE_TAG = "Identity - Auth";

    private static final String TEST_ACCESS_TOKEN = "accessabcdefghijklmnopqrstuvwxyz";

	@Nested
	@DisplayName("인증(로그인) API 테스트")
	class Authenticate {
		@Test
		void 로그인_2XX() throws Exception {
			//given
			User userFixture = UserFixture.USER_FIXTURE_1.create();

			AuthenticateUserRequest requestDto = new AuthenticateUserRequest(userFixture.getLoginId(), userFixture.getPassword());
			AuthenticateUserResponse responseDto = new AuthenticateUserResponse(TEST_ACCESS_TOKEN);

			Mockito.when(authService.authenticate(requestDto))
				.thenReturn(responseDto);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/login")
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON)

			);

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").value(JwtConstants.BEARER_PREFIX + TEST_ACCESS_TOKEN))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.summary("로그인")
							.description("## 로그인 기능\n"
								+ "### 사용법 \n"
								+ "- 필드의 validation을 확인해주세요.\n"
								+ "- Swagger 우측 상단의 Authorize 버튼을 클릭 후 복사한 토큰(Bearer {token} 형식)을 붙여넣고 저장합니다.\n"
								+ "### 필독 \n"
								+ "- accessToken이란? 권한이 필요한 API에 함께 보내야되는 인증 토큰입니다.\n"
							)
							.requestSchema(Schema.schema(AuthenticateUserRequest.class.getSimpleName()))
							.requestFields(
								fieldWithPath("loginId").description("아이디는 영문 4자리 이상입니다.").type(JsonFieldType.STRING),
								fieldWithPath("password").description("비밀번호는 특수문자를 포함한 영문과 숫자 8자리 이상입니다.").type(JsonFieldType.STRING)
							)
							.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
							.responseFields(
								fieldWithPath("message").description("성공 응답 메세지입니다.").type(JsonFieldType.STRING),
								fieldWithPath("data").description("인증 토큰입니다.").type(JsonFieldType.STRING)
							)
							.build()
						)
					)
				);
		}

		@Test
		void 로그인_4XX_요청_데이터_유효성_검사_실패() throws Exception {
		    //given
			String errorMessage = "password" + BASE_FIELD_ERROR_MESSAGE;

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUserRequest requestDto = new AuthenticateUserRequest(userFixture.getLoginId(), "1234");

		    //when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/login")
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON)
			);

		    //then
			actions
				.andExpect(status().isBadRequest())
				.andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.requestSchema(Schema.schema(AuthenticateUserRequest.class.getSimpleName()))
							.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
							.build())
					)
				);
		}

		@Test
		void 로그인_4XX_아이디_존재하지_않음() throws Exception {
		    //given
			String errorMessage = "아이디 혹은 비밀번호가 일치하지 않습니다.";

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUserRequest requestDto = new AuthenticateUserRequest(userFixture.getLoginId(), userFixture.getPassword());

			Mockito.doThrow(new CustomException(HttpStatus.BAD_REQUEST, errorMessage))
				.when(authService).authenticate(requestDto);

		    //when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/login")
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON)
			);

		    //then
			actions
				.andExpect(status().isBadRequest())
				.andExpect(result -> Assertions.assertInstanceOf(CustomException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.requestSchema(Schema.schema(AuthenticateUserRequest.class.getSimpleName()))
							.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
							.build())
					)
				);
		}

		@Test
		void 로그인_4XX_비밀번호가_올바르지_않음() throws Exception {
		    //given
			String errorMessage = "아이디 혹은 비밀번호가 일치하지 않습니다.";

			User userFixture = UserFixture.USER_FIXTURE_1.create();
			AuthenticateUserRequest requestDto = new AuthenticateUserRequest(userFixture.getLoginId(), "wrong1234@");

			Mockito.doThrow(new CustomException(HttpStatus.BAD_REQUEST, errorMessage))
				.when(authService).authenticate(requestDto);

		    //when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/login")
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON)
			);

		    //then
			actions
				.andExpect(status().isBadRequest())
				.andExpect(result -> Assertions.assertInstanceOf(CustomException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
						ResourceDocumentation.resource(ResourceSnippetParameters.builder()
							.tag(BASE_TAG)
							.requestSchema(Schema.schema(AuthenticateUserRequest.class.getSimpleName()))
							.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
							.build())
					)
				);
		}
	}
}