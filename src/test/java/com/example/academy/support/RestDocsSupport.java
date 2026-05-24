package com.example.academy.support;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.academy.common.presentation.HealthCheckController;
import com.example.academy.support.config.RestDocsConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = {
	HealthCheckController.class,
})
@Import({
	RestDocsConfig.class
})
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public abstract class RestDocsSupport {

	protected static final String BASE_SUCCESS_MESSAGE = "OK";
	protected static final String BASE_FIELD_ERROR_MESSAGE = "의 필드 값 유효하지 않습니다.";

	@Autowired
	protected RestDocumentationResultHandler restDocsHandler;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@MockitoBean
	protected JpaMetamodelMappingContext jpaMetamodelMappingContext;

	protected String readMarkdown(String path) {
		ClassPathResource resource = new ClassPathResource(path);
		try {
			return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			fail("문서를 읽어들이는 도중 예외가 발생했습니다. : " + path);
			return null;
		}
	}
}

