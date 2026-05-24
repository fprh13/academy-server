package com.example.academy.common.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.academy.common.presentation.dto.ApiResponse;
import com.example.academy.support.RestDocsSupport;

class HealthCheckControllerTest extends RestDocsSupport {

    @Test
    void 서버_상태_체크_2XX() throws Exception {
        //given & when
        ResultActions actions = mockMvc.perform(get("/health"));

        //then
        actions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value(BASE_SUCCESS_MESSAGE))
			.andExpect(jsonPath("$.data").isEmpty())
			.andDo(restDocsHandler.document(ResourceDocumentation.resource(ResourceSnippetParameters.builder()
					.tag("[필독] API 명세서 가이드")
					.summary("API 사용법")
					.description("") // TODO: 사용 방식에 대해 작성합니다.
					.responseSchema(Schema.schema(ApiResponse.class.getSimpleName()))
					.build())
				)
			);
    }
}