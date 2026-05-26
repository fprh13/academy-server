package com.example.academy.enrollment.presentation;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.example.academy.common.exception.ConflictException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.common.presentation.dto.ApiErrorResponse;
import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.course.domain.Course;
import com.example.academy.enrollment.presentation.dto.request.ApplyEnrollmentRequest;
import com.example.academy.support.RestDocsSupport;

class EnrollmentControllerTest extends RestDocsSupport {

	private static final String BASE_URI = "/enrollments";
	private static final String BASE_TAG = "Enrollment";

	@Nested
	@DisplayName("수강 신청 API 테스트")
	class ApplyEnrollmentTest {
		@Test
		void 수강_신청_2XX() throws Exception {
			// given
			ApplyEnrollmentRequest requestDto = new ApplyEnrollmentRequest(1L);
			Mockito.when(enrollmentService.apply(anyLong(), anyLong()))
				.thenReturn(1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").value(1L))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("수강 신청")
						.description("## 수강 신청 기능 \n"
							+ "### 사용법 \n"
							+ "- 인증된 수강생이 모집 중인 강의에 신청합니다.\n"
							+ "- 정원이 가득 찬 강의는 신청할 수 없습니다.\n"
						)
						.requestSchema(Schema.schema(ApplyEnrollmentRequest.class.getSimpleName()))
						.requestFields(
							fieldWithPath("courseId").description("수강 신청할 강의 식별자입니다.").type(JsonFieldType.NUMBER)
						)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.responseFields(
							fieldWithPath("message").description("성공 응답 메세지입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data").description("생성된 수강 신청 식별자입니다.").type(JsonFieldType.NUMBER)
						)
						.build())
				));
		}

		@Test
		void 수강_신청_4XX_요청_데이터_유효성_검사_실패() throws Exception {
			// given
			String errorMessage = "courseId" + BASE_FIELD_ERROR_MESSAGE;
			ApplyEnrollmentRequest requestDto = new ApplyEnrollmentRequest(null);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			// then
			Mockito.verify(enrollmentService, Mockito.never()).apply(anyLong(), anyLong());
			actions
				.andExpect(status().isBadRequest())
				.andExpect(result -> Assertions.assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.requestSchema(Schema.schema(ApplyEnrollmentRequest.class.getSimpleName()))
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_신청_4XX_강의_없음() throws Exception {
			// given
			String errorMessage = Course.class.getSimpleName() + "을(를) 찾을 수 없습니다.";
			ApplyEnrollmentRequest requestDto = new ApplyEnrollmentRequest(999L);

			Mockito.doThrow(new NotFoundException(Course.class))
				.when(enrollmentService)
				.apply(anyLong(), anyLong());

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isNotFound())
				.andExpect(result -> Assertions.assertInstanceOf(NotFoundException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.requestSchema(Schema.schema(ApplyEnrollmentRequest.class.getSimpleName()))
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_신청_4XX_정원_초과() throws Exception {
			// given
			String errorMessage = "정원이 가득 찼습니다.";
			ApplyEnrollmentRequest requestDto = new ApplyEnrollmentRequest(1L);

			Mockito.doThrow(new ConflictException(errorMessage))
				.when(enrollmentService)
				.apply(anyLong(), anyLong());

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI)
					.content(objectMapper.writeValueAsString(requestDto))
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isConflict())
				.andExpect(result -> Assertions.assertInstanceOf(ConflictException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.requestSchema(Schema.schema(ApplyEnrollmentRequest.class.getSimpleName()))
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}
	}
}
