package com.example.academy.course.presentation;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

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
import com.example.academy.common.exception.NotFoundException;
import com.example.academy.common.presentation.dto.ApiErrorResponse;
import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.common.presentation.dto.PagingResponse;
import com.example.academy.course.domain.Course;
import com.example.academy.course.presentation.dto.request.RegisterCourseRequest;
import com.example.academy.course.presentation.dto.response.CourseDetailResponse;
import com.example.academy.course.presentation.dto.response.CourseSummaryResponse;
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

	@Nested
	@DisplayName("강의 상세조회 API 테스트")
	class GetCourseDetailTest {
		@Test
		void 강의_상세_조회_2XX() throws Exception {
			//given
			Long courseId = 1L;
			CourseDetailResponse responseDto = createCourseDetailResponse(courseId);

			Mockito.when(courseService.getCourseDetail(courseId))
				.thenReturn(responseDto);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/{courseId}", courseId)
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data.courseId").value(responseDto.courseId()))
				.andExpect(jsonPath("$.data.title").value(responseDto.title()))
				.andExpect(jsonPath("$.data.enrollmentCount").value(responseDto.enrollmentCount()))
				.andExpect(jsonPath("$.data.creatorInfo.creatorId").value(responseDto.creatorInfo().creatorId()))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("강의 상세 조회")
						.description("## 강의 상세 기능")
						.pathParameters(
							parameterWithName("courseId").description("조회할 강의의 PK입니다.")
						)
						.responseSchema(Schema.schema(CourseDetailResponse.class.getSimpleName()))
						.responseFields(
							fieldWithPath("message").description("성공 응답 메세지입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.courseId").description("강의 식별자입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.title").description("강의 제목입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.description").description("강의 설명입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.price").description("강의 가격입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.maxCapacity").description("최대 수강 정원입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.enrollmentCount").description("현재 신청 인원입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.startDate").description("수강 시작일입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.endDate").description("수강 종료일입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.creatorInfo.creatorId").description("강사 식별자입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.creatorInfo.creatorName").description("강사 이름입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.creatorInfo.creatorEmail").description("강사 이메일입니다.").type(JsonFieldType.STRING)
						)
						.build())
				));
		}

		@Test
		void 강의_상세조회_4XX_NOTFOUND() throws Exception {
			//given
			String errorMessage = Course.class.getSimpleName() + "을(를) 찾을 수 없습니다.";
			Long courseId = 999L;

			Mockito.doThrow(new NotFoundException(Course.class))
				.when(courseService)
				.getCourseDetail(courseId);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI + "/{courseId}", courseId)
					.contentType(MediaType.APPLICATION_JSON));

			//then
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
	}

	@Nested
	@DisplayName("강의 목록 조회 API 테스트")
	class GetCoursesTest {
		@Test
		void 강의_목록_조회_2XX() throws Exception {
			//given
			String state = "open";
			PagingResponse<CourseSummaryResponse> responseDto = createCoursePagingResponse();

			Mockito.when(courseService.getCourses(eq(state), any()))
				.thenReturn(responseDto);

			//when
			ResultActions actions = mockMvc.perform(
				get(BASE_URI)
					.queryParam("state", state)
					.queryParam("page", "1")
					.queryParam("size", "10")
					.queryParam("sort", "deadline")
					.contentType(MediaType.APPLICATION_JSON));

			//then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
				.andExpect(jsonPath("$.data.content[0].courseId").value(1L))
				.andExpect(jsonPath("$.data.content[1].courseId").value(2L))
				.andExpect(jsonPath("$.data.content[2].courseId").value(3L))
				.andExpect(jsonPath("$.data.content[3].courseId").value(4L))
				.andExpect(jsonPath("$.data.content[4].courseId").value(5L))
				.andExpect(jsonPath("$.data.content[5].courseId").value(6L))
				.andExpect(jsonPath("$.data.content[6].courseId").value(7L))
				.andExpect(jsonPath("$.data.content[7].courseId").value(8L))
				.andExpect(jsonPath("$.data.content[8].courseId").value(9L))
				.andExpect(jsonPath("$.data.content[9].courseId").value(10L))
				.andExpect(jsonPath("$.data.page.number").value(1))
				.andExpect(jsonPath("$.data.page.totalElements").value(15))
				.andDo(restDocsHandler.document(
					ResourceDocumentation.resource(ResourceSnippetParameters.builder()
						.tag(BASE_TAG)
						.summary("강의 목록 조회")
						.description("## 강의 목록 조회 기능 \n"
							+ "### 사용법 \n"
							+ "- 상태 조건과 페이지 조건으로 강의 목록을 조회합니다.\n"
							+ "- state를 생략하면 모집중과 마감 강의를 함께 조회합니다.\n"
						)
						.queryParameters(
							parameterWithName("state").description("강의 상태 필터입니다. open이면 모집중 강의만 조회합니다.").optional(),
							parameterWithName("page").description("조회할 페이지 번호입니다. 1부터 시작합니다.").optional(),
							parameterWithName("size").description("페이지 크기입니다. 기본값은 10입니다.").optional(),
							parameterWithName("sort").description("정렬 기준입니다. deadline이면 종료일 오름차순입니다.").optional()
						)
						.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
						.responseFields(
							fieldWithPath("message").description("성공 응답 메세지입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.content[].courseId").description("강의 식별자입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.content[].creatorName").description("강사 이름입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.content[].title").description("강의 제목입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.content[].price").description("강의 가격입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.content[].maxCapacity").description("최대 수강 정원입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.content[].enrollmentCount").description("현재 신청 인원입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.content[].startDate").description("수강 시작일입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.content[].endDate").description("수강 종료일입니다.").type(JsonFieldType.STRING),
							fieldWithPath("data.page.number").description("현재 페이지 번호입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.page.size").description("페이지 크기입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.page.totalElements").description("전체 강의 수입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.page.totalPages").description("전체 페이지 수입니다.").type(JsonFieldType.NUMBER),
							fieldWithPath("data.page.hasNext").description("다음 페이지 존재 여부입니다.").type(JsonFieldType.BOOLEAN),
							fieldWithPath("data.page.hasPrevious").description("이전 페이지 존재 여부입니다.").type(JsonFieldType.BOOLEAN)
						)
						.build())
				));
		}
	}

	private CourseDetailResponse createCourseDetailResponse(Long courseId) {
		return new CourseDetailResponse(
			courseId,
			"자바 입문",
			"자바 기초 문법을 학습하는 강의입니다.",
			100000,
			30,
			7,
			LocalDate.of(2026, 6, 1),
			LocalDate.of(2026, 6, 30),
			new CourseDetailResponse.CreatorInfo(
				1L,
				"홍길동",
				"creator@test.com"
			)
		);
	}

	private PagingResponse<CourseSummaryResponse> createCoursePagingResponse() {
		return new PagingResponse<>(
			List.of(
				createCourseSummaryResponse(1L),
				createCourseSummaryResponse(2L),
				createCourseSummaryResponse(3L),
				createCourseSummaryResponse(4L),
				createCourseSummaryResponse(5L),
				createCourseSummaryResponse(6L),
				createCourseSummaryResponse(7L),
				createCourseSummaryResponse(8L),
				createCourseSummaryResponse(9L),
				createCourseSummaryResponse(10L)
			),
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

	private CourseSummaryResponse createCourseSummaryResponse(Long courseId) {
		return new CourseSummaryResponse(
			courseId,
			"홍길동",
			"자바 입문",
			100000,
			30,
			7,
			LocalDate.of(2026, 5, 1),
			LocalDate.of(2026, 6, 30)
		);
	}
}
