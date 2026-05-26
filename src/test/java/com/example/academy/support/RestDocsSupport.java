package com.example.academy.support;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.example.academy.common.infrastructure.config.SecurityConfig;
import com.example.academy.common.presentation.HealthCheckController;
import com.example.academy.course.application.CourseService;
import com.example.academy.course.presentation.CourseController;
import com.example.academy.enrollment.application.EnrollmentService;
import com.example.academy.enrollment.presentation.EnrollmentController;
import com.example.academy.identity.application.AuthService;
import com.example.academy.identity.application.UserService;
import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;
import com.example.academy.identity.infrastructure.jwt.JwtTokenProvider;
import com.example.academy.identity.infrastructure.security.AccessDeniedHandlerImpl;
import com.example.academy.identity.infrastructure.security.AuthenticationEntryPointImpl;
import com.example.academy.identity.presentation.AuthController;
import com.example.academy.identity.presentation.UserController;
import com.example.academy.identity.presentation.resolver.AuthUserResolver;
import com.example.academy.support.config.RestDocsConfig;
import com.example.academy.support.fixture.UserFixture;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = {
	HealthCheckController.class,
	UserController.class,
	AuthController.class,
	CourseController.class,
	EnrollmentController.class,
})
@Import({
	SecurityConfig.class,
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

	@MockitoBean
	protected JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	protected AuthenticationEntryPointImpl authenticationEntryPoint;

	@MockitoBean
	protected AccessDeniedHandlerImpl accessDeniedHandler;

	@MockitoBean
	protected AuthUserResolver authUserResolver;

	@MockitoBean
	protected UserRepository userRepository;

	@MockitoBean
	protected UserService userService;

	@MockitoBean
	protected AuthService authService;

	@MockitoBean
	protected CourseService courseService;

	@MockitoBean
	protected EnrollmentService enrollmentService;

	@BeforeEach
	void setUp() {
		User userFixture = UserFixture.USER_FIXTURE_1.create();
		ReflectionTestUtils.setField(userFixture, "id", 1L);

		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(userFixture.getLoginId(), null, List.of())
		);

		Mockito.when(userRepository.findByLoginId(userFixture.getLoginId()))
			.thenReturn(Optional.of(userFixture));
		Mockito.when(authUserResolver.resolveArgument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
			.thenReturn(userFixture);
	}

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
