package com.epam.gymcrmspringboot.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		// Base64 for a 32-byte key, suitable for HS256.
		String secret = "MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=";
		jwtUtil = new JwtUtil(secret, 60_000L);
	}

	@Test
	@DisplayName("Should generate token and extract username/authorities")
	void shouldGenerateAndParseToken() {
		List<GrantedAuthority> authorities = List.of(
				new SimpleGrantedAuthority("ROLE_TRAINER"),
				new SimpleGrantedAuthority("ROLE_TRAINEE")
		);

		String token = jwtUtil.generateToken("john.doe", authorities);

		assertNotNull(token);
		assertEquals("john.doe", jwtUtil.extractUsername(token));
		List<GrantedAuthority> extracted = jwtUtil.extractAuthorities(token);
		assertEquals(2, extracted.size());
		assertTrue(extracted.stream().anyMatch(a -> "ROLE_TRAINER".equals(a.getAuthority())));
		assertTrue(extracted.stream().anyMatch(a -> "ROLE_TRAINEE".equals(a.getAuthority())));
		assertTrue(jwtUtil.isTokenValid(token));
	}

	@Test
	@DisplayName("Should deduplicate duplicate authorities when generating token")
	void shouldDeduplicateAuthorities() {
		List<GrantedAuthority> authorities = List.of(
				new SimpleGrantedAuthority("ROLE_TRAINEE"),
				new SimpleGrantedAuthority("ROLE_TRAINEE")
		);

		String token = jwtUtil.generateToken("jane.doe", authorities);
		List<GrantedAuthority> extracted = jwtUtil.extractAuthorities(token);

		assertEquals(1, extracted.size());
		assertEquals("ROLE_TRAINEE", extracted.get(0).getAuthority());
	}

	@Test
	@DisplayName("Should filter blank roles while extracting authorities")
	void shouldFilterBlankRolesOnExtraction() {
		String secret = "MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=";
		SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		Instant now = Instant.now();
		String token = Jwts.builder()
				.subject("user")
				.claim("roles", List.of("", "  ", "ROLE_TRAINEE"))
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(60)))
				.signWith(key)
				.compact();

		List<GrantedAuthority> extracted = jwtUtil.extractAuthorities(token);

		assertEquals(1, extracted.size());
		assertEquals("ROLE_TRAINEE", extracted.get(0).getAuthority());
	}

	@Test
	@DisplayName("Should throw when trying to generate token without roles")
	void shouldThrowWhenGeneratingWithoutRoles() {
		assertThrows(IllegalArgumentException.class,
				() -> jwtUtil.generateToken("john.doe", List.of()));
		assertThrows(IllegalArgumentException.class,
				() -> jwtUtil.generateToken("john.doe", null));
	}

	@Test
	@DisplayName("Should throw for malformed token")
	void shouldThrowForMalformedToken() {
		assertThrows(RuntimeException.class, () -> jwtUtil.extractUsername("malformed-token"));
		assertThrows(RuntimeException.class, () -> jwtUtil.isTokenValid("malformed-token"));
	}
}


