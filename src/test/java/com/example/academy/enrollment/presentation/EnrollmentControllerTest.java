package com.example.academy.enrollment.presentation;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
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
import com.example.academy.common.exception.ForbiddenException;
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.common.exception.BadRequestException;
import com.example.academy.common.presentation.dto.ApiErrorResponse;
import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.course.domain.Course;
import com.example.academy.enrollment.domain.Enrollment;
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

	@Nested
	@DisplayName("수강 확정 API 테스트")
	class ConfirmEnrollmentTest {
		@Test
		void 수강_확정_2XX() throws Exception {
			// given
			Long enrollmentId = 1L;
			Mockito.doNothing().when(enrollmentService).confirm(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/confirm", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("수강 확정")
						.description("## 수강 확정 기능 \n"
							+ "### 사용법 \n"
							+ "- 본인의 결제 대기 상태 수강 신청을 확정합니다.\n"
							+ "- 이미 확정되었거나 본인 신청이 아니면 실패합니다.\n"
						)
						.pathParameters(
							parameterWithName("enrollmentId").description("확정할 수강 신청의 PK입니다.")
						)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_확정_4XX_수강신청_없음() throws Exception {
			// given
			Long enrollmentId = 999L;
			String errorMessage = Enrollment.class.getSimpleName() + "을(를) 찾을 수 없습니다.";

			Mockito.doThrow(new NotFoundException(Enrollment.class))
				.when(enrollmentService)
				.confirm(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/confirm", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isNotFound())
				.andExpect(result -> Assertions.assertInstanceOf(NotFoundException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_확정_4XX_본인_신청_아님() throws Exception {
			// given
			Long enrollmentId = 1L;
			String errorMessage = "접근 권한이 없습니다.";

			Mockito.doThrow(new ForbiddenException())
				.when(enrollmentService)
				.confirm(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/confirm", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isForbidden())
				.andExpect(result -> Assertions.assertInstanceOf(ForbiddenException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_확정_4XX_이미_확정됨() throws Exception {
			// given
			Long enrollmentId = 1L;
			String errorMessage = "결제 대기 상태의 수강 신청만 결제 확정할 수 있습니다.";

			Mockito.doThrow(new ConflictException(errorMessage))
				.when(enrollmentService)
				.confirm(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/confirm", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isConflict())
				.andExpect(result -> Assertions.assertInstanceOf(ConflictException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}
	}

	@Nested
	@DisplayName("수강 신청 취소 API 테스트")
	class CancelEnrollmentTest {
		@Test
		void 수강_신청_취소_2XX() throws Exception {
			// given
			Long enrollmentId = 1L;
			Mockito.doNothing().when(enrollmentService).cancel(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/cancel", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("수강 신청 취소")
						.description("## 수강 신청 취소 기능 \n"
							+ "### 사용법 \n"
							+ "- 본인의 수강 신청을 취소합니다.\n"
							+ "- 결제 대기 상태 신청만 취소할 수 있습니다.\n"
						)
						.pathParameters(
							parameterWithName("enrollmentId").description("취소할 수강 신청의 PK입니다.")
						)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_신청_취소_4XX_수강신청_없음() throws Exception {
			// given
			Long enrollmentId = 999L;
			String errorMessage = Enrollment.class.getSimpleName() + "을(를) 찾을 수 없습니다.";

			Mockito.doThrow(new NotFoundException(Enrollment.class))
				.when(enrollmentService)
				.cancel(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/cancel", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isNotFound())
				.andExpect(result -> Assertions.assertInstanceOf(NotFoundException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_신청_취소_4XX_본인_신청_아님() throws Exception {
			// given
			Long enrollmentId = 1L;
			String errorMessage = "접근 권한이 없습니다.";

			Mockito.doThrow(new ForbiddenException())
				.when(enrollmentService)
				.cancel(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/cancel", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isForbidden())
				.andExpect(result -> Assertions.assertInstanceOf(ForbiddenException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}
	}
}
