package com.example.academy.identity.infrastructure.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.academy.common.exception.UnauthorizedException;
import com.example.academy.identity.domain.user.User;
import com.example.academy.support.fixture.UserFixture;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_ACCESS_SECRET = "accessabcdefghijklmnopqrstuvwxyz";
    private static final Long TEST_EXPIRATION_DAYS = 21L;
	private static final Long TEST_EXPIRATION_SECONDS = 60L * 10;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
			TEST_ACCESS_SECRET,
			TEST_EXPIRATION_DAYS
        );
    }

	@Nested
	@DisplayName("엑세스 토큰 생성 기능")
	class CreateAccessToken {
		@Test
		void 엑세스_토큰을_생성한다() {
			//given
			User user = UserFixture.USER_FIXTURE_1.create();
			String claimKey = "role";
			long expirationMs = TEST_EXPIRATION_DAYS * 24 * 60 * 60 * 1_000L;

			Instant nowInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
			Date now = Date.from(nowInstant);

			//when
			String accessToken = jwtTokenProvider.createAccessToken(user, now);

			//then
			Claims claims = getClaims(accessToken, TEST_ACCESS_SECRET);

			assertAll(
				() -> assertThat(accessToken).isNotNull(),
				() -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId()),
				() -> assertThat(claims.get(claimKey)).isEqualTo(user.getRole().getKey()),
				() -> assertThat(claims.getIssuedAt()).isEqualTo(now),
				() -> assertThat(claims.getExpiration()).isEqualTo(new Date(now.getTime() + expirationMs))
			);
		}
	}

	@Nested
	@DisplayName("엑세스 토큰 Claims 추출 기능")
	class GetAccessTokenClaims {
		@Test
		void AccessToken_Claims을_추출한다() {
			//given
			String claimKey = "role";
			User user = UserFixture.USER_FIXTURE_1.create();

			long expirationMs = TEST_EXPIRATION_DAYS * 24 * 60 * 60 * 1_000L;
			Instant nowInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
			Date now = Date.from(nowInstant);

			String accessToken = jwtTokenProvider.createAccessToken(user, now);

			//when
			Claims claims = jwtTokenProvider.parseAccessToken(accessToken);

			//then
			assertAll(
				() -> assertThat(accessToken).isNotNull(),
				() -> assertThat(claims.getSubject()).isEqualTo(user.getLoginId()),
				() -> assertThat(claims.get(claimKey)).isEqualTo(user.getRole().getKey()),
				() -> assertThat(claims.getIssuedAt()).isEqualTo(now),
				() -> assertThat(claims.getExpiration()).isEqualTo(new Date(now.getTime() + expirationMs))
			);
		}
		
		@Test
		void 잘못된_AccessToken_형식으로_파싱에_실패한_경우_예외를_반환한다() {
		    //given
			String accessToken = jwtTokenProvider.createAccessToken(UserFixture.USER_FIXTURE_1.create(), new Date());
		    
		    //when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken("hacking" + accessToken))
				.isInstanceOf(UnauthorizedException.class);
		}
		
		@Test
		void AccessToken이_null인_경우_예외를_반환한다() {
		    //given
			String accessToken = null;
		    
		    //when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		    
		}
		
		@Test
		void AccessToken이_공백인_경우_예외를_반환한다() {
		    //given
		    String accessToken = "";
			
		    //when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		    
		}
		
		@Test
		void Claims의_subject가_없다면_예외를_반환한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			String claimsKey = "role";

			String accessToken = Jwts.builder()
				.claim(claimsKey, user.getRole().getKey())
				.compact();

			//when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		}
		
		@Test
		void Claims의_권한이_누락되면_예외를_반환한다() {
		    //given
			String accessToken = Jwts.builder()
				.subject("testLoginId")
				.compact();
		    
		    //when & then
			assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(accessToken))
				.isInstanceOf(UnauthorizedException.class);
		}
	}
	
    private Claims getClaims(String token, String secret) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}