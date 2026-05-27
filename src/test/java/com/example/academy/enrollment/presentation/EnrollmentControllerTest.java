package com.example.academy.enrollment.presentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

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
import com.example.academy.common.presentation.dto.PagingResponse;
import com.example.academy.course.domain.Course;
import com.example.academy.enrollment.domain.Enrollment;
import com.example.academy.enrollment.presentation.dto.request.ApplyEnrollmentRequest;
import com.example.academy.enrollment.presentation.dto.response.EnrollmentInfoResponse;
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
							+ "- 정원이 가득 찬 강의이라면 웨이팅(대기열)으로 등록됩니다.\n"
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
				.andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,
					result.getResolvedException()))
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
				.andExpect(
					result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()))
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
				.andExpect(
					result -> assertInstanceOf(ConflictException.class, result.getResolvedException()))
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
				.andExpect(
					result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()))
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
				.andExpect(
					result -> assertInstanceOf(ForbiddenException.class, result.getResolvedException()))
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
				.andExpect(
					result -> assertInstanceOf(ConflictException.class, result.getResolvedException()))
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
				.andExpect(
					result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()))
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
				.andExpect(
					result -> assertInstanceOf(ForbiddenException.class, result.getResolvedException()))
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
	@DisplayName("수강 확정 취소 API 테스트")
	class RefundEnrollmentTest {
		@Test
		void 수강_확정_취소_2XX() throws Exception {
			// given
			Long enrollmentId = 1L;
			Mockito.doNothing().when(enrollmentService).cancelConfirm(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/refund", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("수강 확정 취소")
						.description("## 수강 확정 취소 기능 \n"
							+ "### 사용법 \n"
							+ "- 본인의 결제 확정 상태 수강 신청을 취소합니다.\n"
							+ "- 결제 후 7일 이내에만 취소할 수 있습니다.\n"
						)
						.pathParameters(
							parameterWithName("enrollmentId").description("수강 확정 취소할 수강 신청의 PK입니다.")
						)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_확정_취소_4XX_수강신청_없음() throws Exception {
			// given
			Long enrollmentId = 999L;
			String errorMessage = Enrollment.class.getSimpleName() + "을(를) 찾을 수 없습니다.";

			Mockito.doThrow(new NotFoundException(Enrollment.class))
				.when(enrollmentService)
				.cancelConfirm(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/refund", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isNotFound())
				.andExpect(
					result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_확정_취소_4XX_본인_신청_아님() throws Exception {
			// given
			Long enrollmentId = 1L;
			String errorMessage = "접근 권한이 없습니다.";

			Mockito.doThrow(new ForbiddenException())
				.when(enrollmentService)
				.cancelConfirm(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/refund", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isForbidden())
				.andExpect(
					result -> assertInstanceOf(ForbiddenException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_확정_취소_4XX_결제_확정_상태_아님() throws Exception {
			// given
			Long enrollmentId = 1L;
			String errorMessage = "결제 확정 상태의 수강 신청만 취소할 수 있습니다.";

			Mockito.doThrow(new ConflictException(errorMessage))
				.when(enrollmentService)
				.cancelConfirm(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/refund", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isConflict())
				.andExpect(
					result -> assertInstanceOf(ConflictException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_확정_취소_4XX_취소_가능_기간_초과() throws Exception {
			// given
			Long enrollmentId = 1L;
			String errorMessage = "결제 후 7일이 지나 취소할 수 없습니다.";

			Mockito.doThrow(new BadRequestException(errorMessage))
				.when(enrollmentService)
				.cancelConfirm(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/refund", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isBadRequest())
				.andExpect(
					result -> assertInstanceOf(BadRequestException.class, result.getResolvedException()))
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
	@DisplayName("웨이팅 취소 API 테스트")
	class CancelWaitingEnrollmentTest {
		@Test
		void 웨이팅_취소_2XX() throws Exception {
			// given
			Long enrollmentId = 1L;
			Mockito.doNothing().when(enrollmentService).cancelWaiting(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/wait-cancel", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data").isEmpty())
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("웨이팅 취소")
						.description("## 웨이팅 취소 기능 \n"
							+ "### 사용법 \n"
							+ "- 본인의 웨이팅 상태 수강 신청을 취소합니다.\n"
							+ "- 웨이팅 상태 수강 신청만 취소할 수 있습니다.\n"
						)
						.pathParameters(
							parameterWithName("enrollmentId").description("웨이팅 취소할 수강 신청의 PK입니다.")
						)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 웨이팅_취소_4XX_수강신청_없음() throws Exception {
			// given
			Long enrollmentId = 999L;
			String errorMessage = Enrollment.class.getSimpleName() + "을(를) 찾을 수 없습니다.";

			Mockito.doThrow(new NotFoundException(Enrollment.class))
				.when(enrollmentService)
				.cancelWaiting(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/wait-cancel", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isNotFound())
				.andExpect(
					result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 웨이팅_취소_4XX_본인_신청_아님() throws Exception {
			// given
			Long enrollmentId = 1L;
			String errorMessage = "접근 권한이 없습니다.";

			Mockito.doThrow(new ForbiddenException())
				.when(enrollmentService)
				.cancelWaiting(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/wait-cancel", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isForbidden())
				.andExpect(
					result -> assertInstanceOf(ForbiddenException.class, result.getResolvedException()))
				.andExpect(jsonPath("$.message").value(errorMessage))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiErrorResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 웨이팅_취소_4XX_웨이팅_상태_아님() throws Exception {
			// given
			Long enrollmentId = 1L;
			String errorMessage = "웨이팅 상태의 수강 신청만 취소할 수 있습니다.";

			Mockito.doThrow(new ConflictException(errorMessage))
				.when(enrollmentService)
				.cancelWaiting(enrollmentId, 1L);

			// when
			ResultActions actions = mockMvc.perform(
				post(BASE_URI + "/{enrollmentId}/wait-cancel", enrollmentId)
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isConflict())
				.andExpect(
					result -> assertInstanceOf(ConflictException.class, result.getResolvedException()))
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
	@DisplayName("수강 신청 목록 조회 API 테스트")
	class GetEnrollmentsTest {
		@Test
		void 수강_신청_목록_조회_2XX() throws Exception {
			// given
			PagingResponse<EnrollmentInfoResponse> responseDto = createEnrollmentPagingResponse(
				List.of(
					createPaddingEnrollmentInfoResponse(1L),
					createPaddingEnrollmentInfoResponse(2L),
					createPaddingEnrollmentInfoResponse(3L),
					createPaddingEnrollmentInfoResponse(4L),
					createPaddingEnrollmentInfoResponse(5L),
					createConFirmedEnrollmentInfoResponse(6L),
					createConFirmedEnrollmentInfoResponse(7L),
					createConFirmedEnrollmentInfoResponse(8L),
					createConFirmedEnrollmentInfoResponse(9L),
					createConFirmedEnrollmentInfoResponse(10L)
				)
			);

			Mockito.when(enrollmentService.gets(Mockito.any(), Mockito.isNull(), Mockito.eq(1L)))
				.thenReturn(responseDto);

			// when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI)
					.queryParam("page", "1")
					.queryParam("size", "10")
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data.content[0].enrollmentId").value(1L))
				.andExpect(jsonPath("$.data.content[0].state").value("PADDING"))
				.andExpect(jsonPath("$.data.content[0].courseInfo.courseId").value(13L))
				.andExpect(jsonPath("$.data.page.number").value(1))
				.andExpect(jsonPath("$.data.page.totalElements").value(15))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("수강 신청 목록 조회")
						.description("## 수강 신청 목록 조회 기능 \n"
							+ "### 사용법 \n"
							+ "- 상태 조건과 페이지 조건트으로 본인의 수강 신청 목록을 조회합니다.\n"
							+ "- state를 생략하면 결제 대기, 결제 확정, 웨이팅 목록을 함께 조회합니다.\n"
							+ "- state가 confirmed면 결제 확정 목록만 조회합니다.\n"
							+ "- state가 confirmed면 결제 취소 목록만 조회합니다.\n"
							+ "- state가 waiting면 웨이팅 목록만 조회합니다.\n"
						)
						.queryParameters(
							parameterWithName("state").description(
									"수강 신청 상태 필터입니다. 생략하면 수강 대기 확정, 웨이팅만, confirmed면 확정 목록만, cancel이면 취소 목록만, waiting이면 웨이팅 목록만 조회합니다.")
								.optional(),
							parameterWithName("page").description("조회할 페이지 번호입니다. 1부터 시작합니다.").optional(),
							parameterWithName("size").description("페이지 크기입니다. 기본값은 10입니다.").optional()
						)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.responseFields(
							fieldWithPath("message").description("성공 응답 메세지입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.content[].enrollmentId").description("수강 신청 식별자입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.content[].state").description("수강 신청 상태입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.content[].createAt").description("수강 신청 생성 시각입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.content[].paidAt").description("결제 확정 시각입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.content[].courseInfo.courseId").description("강의 식별자입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.content[].courseInfo.courseName").description("강사 이름입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.content[].courseInfo.coursePrice").description("강의 가격입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.page.number").description("현재 페이지 번호입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.page.size").description("페이지 크기입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.page.totalElements").description("전체 수강 신청 수입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.page.totalPages").description("전체 페이지 수입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.page.hasNext").description("다음 페이지 존재 여부입니다.").type(JsonFieldType.BOOLEAN),
							fieldWithPath("data.page.hasPrevious").description("이전 페이지 존재 여부입니다.").type(JsonFieldType.BOOLEAN)
						).build())
				));
		}

		@Test
		void 수강_신청_목록_조회_2XX_확정건_조회() throws Exception {
			// given
			PagingResponse<EnrollmentInfoResponse> responseDto = createEnrollmentPagingResponse(
				List.of(
					createConFirmedEnrollmentInfoResponse(1L),
					createConFirmedEnrollmentInfoResponse(2L),
					createConFirmedEnrollmentInfoResponse(3L),
					createConFirmedEnrollmentInfoResponse(4L),
					createConFirmedEnrollmentInfoResponse(5L),
					createConFirmedEnrollmentInfoResponse(6L),
					createConFirmedEnrollmentInfoResponse(7L),
					createConFirmedEnrollmentInfoResponse(8L),
					createConFirmedEnrollmentInfoResponse(9L),
					createConFirmedEnrollmentInfoResponse(10L)
				)
			);

			Mockito.when(enrollmentService.gets(Mockito.any(), Mockito.anyString(), Mockito.eq(1L)))
				.thenReturn(responseDto);

			// when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI)
					.queryParam("state", "confirmed")
					.queryParam("page", "1")
					.queryParam("size", "10")
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data.content[0].enrollmentId").value(1L))
				.andExpect(jsonPath("$.data.page.number").value(1))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.build())
				));
		}

		@Test
		void 수강_신청_목록_조회_2XX_취소건_조회() throws Exception {
			// given
			PagingResponse<EnrollmentInfoResponse> responseDto = createEnrollmentPagingResponse(
				List.of(
					createCanceledEnrollmentInfoResponse(1L),
					createCanceledEnrollmentInfoResponse(2L),
					createCanceledEnrollmentInfoResponse(3L),
					createCanceledEnrollmentInfoResponse(4L),
					createCanceledEnrollmentInfoResponse(5L),
					createCanceledEnrollmentInfoResponse(6L),
					createCanceledEnrollmentInfoResponse(7L),
					createCanceledEnrollmentInfoResponse(8L),
					createCanceledEnrollmentInfoResponse(9L),
					createCanceledEnrollmentInfoResponse(10L)
				)
			);

			Mockito.when(enrollmentService.gets(Mockito.any(), Mockito.anyString(), Mockito.eq(1L)))
				.thenReturn(responseDto);

			// when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI)
					.queryParam("state", "cancelled")
					.queryParam("page", "1")
					.queryParam("size", "10")
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data.content[0].enrollmentId").value(1L))
				.andExpect(jsonPath("$.data.page.number").value(1))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
				.build())
				));
		}

		@Test
		void 수강_신청_목록_조회_2XX_웨이팅건_조회() throws Exception {
			// given
			PagingResponse<EnrollmentInfoResponse> responseDto = createEnrollmentPagingResponse(
				List.of(
					createWaitingEnrollmentInfoResponse(1L),
					createWaitingEnrollmentInfoResponse(2L),
					createWaitingEnrollmentInfoResponse(3L),
					createWaitingEnrollmentInfoResponse(4L),
					createWaitingEnrollmentInfoResponse(5L),
					createWaitingEnrollmentInfoResponse(6L),
					createWaitingEnrollmentInfoResponse(7L),
					createWaitingEnrollmentInfoResponse(8L),
					createWaitingEnrollmentInfoResponse(9L),
					createWaitingEnrollmentInfoResponse(10L)
				)
			);

			Mockito.when(enrollmentService.gets(Mockito.any(), Mockito.anyString(), Mockito.eq(1L)))
				.thenReturn(responseDto);

			// when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI)
					.queryParam("state", "waiting")
					.queryParam("page", "1")
					.queryParam("size", "10")
					.contentType(MediaType.APPLICATION_JSON));

			// then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data.content[0].enrollmentId").value(1L))
				.andExpect(jsonPath("$.data.page.number").value(1))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.build())
				));
		}
	}

	private PagingResponse<EnrollmentInfoResponse> createEnrollmentPagingResponse(
		List<EnrollmentInfoResponse> enrollmentInfoResponses
	) {
		return new PagingResponse<>(
			enrollmentInfoResponses,
			new PagingResponse.PageMetaData(
				1,
				10,
				15L,
				2,
				true,
				false
			)
		);
	}

	private EnrollmentInfoResponse createConFirmedEnrollmentInfoResponse(Long enrollmentId) {
		return new EnrollmentInfoResponse(
			enrollmentId,
			"CONFIRMED",
			LocalDateTime.of(2026, 6, 2, 10, 0),
			LocalDateTime.of(2026, 6, 2, 10, 5),
			new EnrollmentInfoResponse.CourseInfo(11L, "김확정", 110_000)
		);
	}

	private EnrollmentInfoResponse createCanceledEnrollmentInfoResponse(Long enrollmentId) {
		return new EnrollmentInfoResponse(
			enrollmentId,
			"CANCELED",
			LocalDateTime.of(2026, 6, 2, 10, 0),
			LocalDateTime.of(2026, 6, 2, 10, 5),
			new EnrollmentInfoResponse.CourseInfo(12L, "이취소", 990_000)
		);
	}

	private EnrollmentInfoResponse createWaitingEnrollmentInfoResponse(Long enrollmentId) {
		return new EnrollmentInfoResponse(
			enrollmentId,
			"WAITING",
			LocalDateTime.of(2026, 6, 2, 10, 0),
			LocalDateTime.of(2026, 6, 2, 10, 5),
			new EnrollmentInfoResponse.CourseInfo(12L, "이취소", 990_000)
		);
	}

	private EnrollmentInfoResponse createPaddingEnrollmentInfoResponse(Long enrollmentId) {
		return new EnrollmentInfoResponse(
			enrollmentId,
			"PADDING",
			LocalDateTime.of(2026, 6, 2, 10, 0),
			LocalDateTime.of(2026, 6, 2, 10, 5),
			new EnrollmentInfoResponse.CourseInfo(13L, "박대기", 190_000)
		);
	}
}
