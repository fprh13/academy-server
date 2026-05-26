package com.example.academy.common.infrastructure.config;

import static org.springframework.http.HttpMethod.*;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.academy.identity.infrastructure.security.AccessDeniedHandlerImpl;
import com.example.academy.identity.infrastructure.security.AuthenticationEntryPointImpl;
import com.example.academy.identity.infrastructure.security.JwtAuthorizationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

	private static final String USER_URI = "/users";
	private static final String AUTH_URI = "/auth";
	private static final String COURSE_URI = "/courses";
	private static final String ENROLLMENT_URI = "/enrollments";
	private static final String[] SWAGGER_PATTERNS = {"/swagger-ui/**", "/v3/api-docs/**", "/static/swagger-ui/**"};

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        PathPatternRequestMatcher.Builder mvc = PathPatternRequestMatcher.withDefaults();
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

					//== 전체 공개 ==//
					.requestMatchers(SWAGGER_PATTERNS).permitAll()
					.requestMatchers(mvc.matcher(GET, "/health")).permitAll()

					.requestMatchers(
						mvc.matcher(POST, USER_URI),
						mvc.matcher(GET, USER_URI + "/login-id/exists"),
						mvc.matcher(GET, USER_URI + "/email/exists"),
						mvc.matcher(GET, USER_URI + "/{userId}")
					).permitAll()

					.requestMatchers(
						mvc.matcher(POST, AUTH_URI + "/login")
					).permitAll()

					.requestMatchers(
						mvc.matcher(GET, COURSE_URI),
						mvc.matcher(GET, COURSE_URI + "{courseId}")
					).permitAll()

					//== 인증 필요 ==//
					.requestMatchers(
						mvc.matcher(GET, USER_URI + "/profile"),
						mvc.matcher(PUT, USER_URI)
					).authenticated()

					.requestMatchers(
						mvc.matcher(POST, COURSE_URI)
					).authenticated()

					.requestMatchers(
						mvc.matcher(GET, ENROLLMENT_URI),
						mvc.matcher(POST, ENROLLMENT_URI),
						mvc.matcher(POST, ENROLLMENT_URI + "/{enrollmentId}/confirm"),
						mvc.matcher(POST, ENROLLMENT_URI + "/{enrollmentId}/cancel"),
						mvc.matcher(POST, ENROLLMENT_URI + "/{enrollmentId}/refund")
					).authenticated()

					.anyRequest().permitAll()
                )

                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Set-Cookie");
        configuration.setMaxAge(3_600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
