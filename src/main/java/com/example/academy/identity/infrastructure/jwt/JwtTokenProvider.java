package com.example.academy.identity.infrastructure.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.academy.common.exception.UnauthorizedException;
import com.example.academy.identity.domain.auth.AuthorizationErrorMessages;
import com.example.academy.identity.domain.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
	private static final long MILLIS_PER_SECOND = 1_000L;

    private final SecretKey accessTokenSigningKey;
    private final long accessTokenExpirationSeconds;


    public JwtTokenProvider(
		@Value("${jwt.access-secret-key}") String accessTokenSecret,
		@Value("${jwt.access-token-valid-days}") Long accessTokenExpirationDays
	) {
        this.accessTokenSigningKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationDays * 24 * 60 * 60;
    }

    public String createAccessToken(User user, Date now) {
        return Jwts.builder()
                .subject(user.getLoginId())
                .claim(JwtConstants.AUTHORITIES_CLAIM_KEY, user.getRole().getKey())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpirationSeconds * MILLIS_PER_SECOND))
                .signWith(accessTokenSigningKey)
                .compact();
    }

	public Claims parseAccessToken(String token) {
		Claims claims = parseClaims(token, accessTokenSigningKey);
		validateAccessTokenClaims(claims);
		return claims;
	}

	private Claims parseClaims(String token, SecretKey signingKey) {
		try {
			return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (JwtException | IllegalArgumentException e) {
			throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
		}
	}

	private void validateAccessTokenClaims(Claims claims) {
		String subject = claims.getSubject();
		String role = claims.get(JwtConstants.AUTHORITIES_CLAIM_KEY, String.class);

		if (subject == null || role == null) {
			throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
		}
	}
}
