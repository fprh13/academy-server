package com.example.academy.course.presentation;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.presentation.dto.ApiErrorResponse;
import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.course.presentation.dto.request.RegisterCourseRequest;
import com.example.academy.support.RestDocsSupport;

class CourseControllerTest extends RestDocsSupport {

	private static final String BASE_URI = "/courses";
	private static final String BASE_TAG = "Course";

	@Nested
	@DisplayName("강의 생성 API 테스트")
	class RegisterTest {
		@Test
		void 강의_생성_2XX() throws Exception {
			//given
			RegisterCourseRequest requestDto = new RegisterCourseRequest(
				"자바 입문",
				"자바 기초 문법을 학습하는 강의입니다.",
				100000,
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 6, 30)
			);
			Mockito.when(courseService.registerCourse(any(RegisterCourseRequest.class), anyLong()))
				.thenReturn(1L);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").value(1L))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("강의 등록")
						.description("## 강의 등록 기능 \n"
							+ "### 사용법 \n"
							+ "- 강사 사용자만 사용가능합니다.\n"
						)
						.requestSchema(Schema.schema(RegisterCourseRequest.class.getSimpleName()))
						.requestFields(
							fieldWithPath("title").description("강의 제목입니다.").type(JsonFieldType.STRING),
							fieldWithPath("description").description("강의 설명입니다.").type(JsonFieldType.STRING),
							fieldWithPath("price").description("강의 가격입니다. 0 이상이어야 합니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("maxCapacity").description("최대 수강 정원입니다. 1 이상이어야 합니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("startDate").description("수강 시작일입니다.").type(JsonFieldType.STRING),
							fieldWithPath("endDate").description("수강 종료일입니다.").type(JsonFieldType.STRING)
						)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.responseFields(
							fieldWithPath("message").description("성공 응답 메세지입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data").description("생성한 강의 식별자입니다.").type(JsonFieldType.NUMBER)
						)
						.build())
				));
		}

		@Test
		void 강의_생성_4XX_요청_데이터_유효성_검사_실패() throws Exception {
			//given
			String errorMessage = "title" + BASE_FIELD_ERROR_MESSAGE;
			RegisterCourseRequest requestDto = new RegisterCourseRequest(
				"",
				"자바 기초 문법을 학습하는 강의입니다.",
				100000,
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 6, 30)
			);

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			Mockito.verify(courseService, Mockito.never()).registerCourse(any(), anyLong());
			actions
				.andExpect(status().isBadRequest())
				.andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.requestSchema(Schema.schema(RegisterCourseRequest.class.getSimpleName()))
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 강의_생성_4XX_강사가_아닌_경우() throws Exception {
			//given
			String errorMessage = "접근 권한이 없습니다.";
			RegisterCourseRequest requestDto = new RegisterCourseRequest(
				"자바 입문",
				"자바 기초 문법을 학습하는 강의입니다.",
				100000,
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 6, 30)
			);

			Mockito.doThrow(new ForbiddenException())
				.when(courseService)
				.registerCourse(any(RegisterCourseRequest.class), anyLong());

			//when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isForbidden())
				.andExpect(result -> Assertions.assertInstanceOf(ForbiddenException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.requestSchema(Schema.schema(RegisterCourseRequest.class.getSimpleName()))
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}
	}
}
