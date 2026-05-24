package com.example.academy.identity.infrastructure.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.academy.common.exception.UnauthorizedException;
import com.example.academy.identity.infrastructure.jwt.JwtConstants;
import com.example.academy.identity.infrastructure.jwt.JwtTokenProvider;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String accessToken = resolveAccessToken(request.getHeader(HttpHeaders.AUTHORIZATION));

            if (accessToken != null) {
                authenticate(accessToken);
            }

			filterChain.doFilter(request, response);

        } catch (UnauthorizedException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new AuthenticationException(e.getMessage()) {
            });
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
			log.error("JWT에 예상 못한 예외 발생:  {}", e.getMessage());
			throw e;
        }
    }

    private void authenticate(String accessToken) {
		Claims claims;
        try {
			claims = jwtTokenProvider.parseAccessToken(accessToken);
		} catch (UnauthorizedException e) {
            return;
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                null,
                List.of(new SimpleGrantedAuthority(claims.get(JwtConstants.AUTHORITIES_CLAIM_KEY).toString()))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveAccessToken(String requestAccessTokenInHeader) {
        if (requestAccessTokenInHeader == null) {
            return null;
        }
        if (!requestAccessTokenInHeader.startsWith(JwtConstants.BEARER_PREFIX)) {
            return null;
        }
        return requestAccessTokenInHeader.substring(JwtConstants.BEARER_PREFIX.length());
    }
}
