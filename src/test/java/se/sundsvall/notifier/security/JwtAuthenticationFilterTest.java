package se.sundsvall.notifier.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private JwtAuthenticationFilter filter;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilterInternal_withValidAuthorizationHeader_setsAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
		when(jwtUtil.validateToken("validtoken")).thenReturn(true);
		when(jwtUtil.extractUsername("validtoken")).thenReturn("user@example.com");
		when(jwtUtil.extractRole("validtoken")).thenReturn("USER");

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@example.com");
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_withValidCookie_setsAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn(null);
		when(request.getCookies()).thenReturn(new Cookie[] {
			new Cookie("token", "cookietoken")
		});
		when(jwtUtil.validateToken("cookietoken")).thenReturn(true);
		when(jwtUtil.extractUsername("cookietoken")).thenReturn("user@example.com");
		when(jwtUtil.extractRole("cookietoken")).thenReturn("USER");

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_withOtherCookieOnly_doesNotSetAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn(null);
		when(request.getCookies()).thenReturn(new Cookie[] {
			new Cookie("other", "value")
		});

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_withInvalidToken_doesNotSetAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
		when(jwtUtil.validateToken("invalidtoken")).thenReturn(false);

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_withNoToken_doesNotSetAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn(null);
		when(request.getCookies()).thenReturn(null);

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_withNullRole_setsEmptyAuthorities() throws Exception {
		when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
		when(jwtUtil.validateToken("validtoken")).thenReturn(true);
		when(jwtUtil.extractUsername("validtoken")).thenReturn("user@example.com");
		when(jwtUtil.extractRole("validtoken")).thenReturn(null);

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEmpty();
		verify(filterChain).doFilter(request, response);
	}
}
