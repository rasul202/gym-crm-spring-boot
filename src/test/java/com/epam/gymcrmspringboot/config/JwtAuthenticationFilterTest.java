package com.epam.gymcrmspringboot.config;

import com.epam.gymcrmspringboot.service.AuthenticationService;
import com.epam.gymcrmspringboot.service.JwtTokenRevocationService;
import com.epam.gymcrmspringboot.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private JwtTokenRevocationService jwtTokenRevocationService;

	@Mock
	private FilterChain filterChain;

	private JwtAuthenticationFilter filter;

	@BeforeEach
	void setUp() {
		filter = new JwtAuthenticationFilter(jwtUtil, authenticationService, jwtTokenRevocationService);
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Should continue chain without authentication when Authorization header is absent")
	void shouldContinueWhenAuthorizationHeaderAbsent() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/trainees/john");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(authenticationService.extractTokenFromAuthorizationHeader(null)).thenReturn(null);

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verifyNoInteractions(jwtUtil);
		verifyNoInteractions(jwtTokenRevocationService);
		verify(filterChain).doFilter(any(), any());
	}

	@Test
	@DisplayName("Should populate security context when token is valid")
	void shouldPopulateSecurityContextWhenTokenValid() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/trainees/john");
		request.addHeader("Authorization", "Bearer valid-token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(authenticationService.extractTokenFromAuthorizationHeader("Bearer valid-token")).thenReturn("valid-token");
		when(jwtTokenRevocationService.isTokenRevoked("valid-token")).thenReturn(false);
		when(jwtUtil.extractUsername("valid-token")).thenReturn("john.doe");
		when(jwtUtil.extractAuthorities("valid-token"))
				.thenReturn(List.of(new SimpleGrantedAuthority("ROLE_TRAINEE")));
		when(jwtUtil.isTokenValid("valid-token")).thenReturn(true);

		filter.doFilter(request, response, filterChain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertEquals("john.doe", SecurityContextHolder.getContext().getAuthentication().getName());
		verify(filterChain).doFilter(any(), any());
	}

	@Test
	@DisplayName("Should clear context and continue chain when token parsing fails")
	void shouldClearContextWhenTokenParsingFails() throws ServletException, IOException {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("existing", null, List.of())
		);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/trainees/john");
		request.addHeader("Authorization", "Bearer bad-token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(authenticationService.extractTokenFromAuthorizationHeader("Bearer bad-token")).thenReturn("bad-token");
		when(jwtTokenRevocationService.isTokenRevoked("bad-token")).thenReturn(false);
		when(jwtUtil.extractUsername("bad-token")).thenThrow(new RuntimeException("invalid token"));

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(any(), any());
	}
}
