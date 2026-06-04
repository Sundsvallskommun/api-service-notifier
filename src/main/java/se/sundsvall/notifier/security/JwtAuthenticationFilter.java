package se.sundsvall.notifier.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	public JwtAuthenticationFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		String token = null;

		if (request.getHeader("Authorization") != null) {
			token = request.getHeader("Authorization").substring(7);
		} else if (request.getCookies() != null) {
			for (final Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals("token")) {
					token = cookie.getValue();
					break;
				}
			}
		}

		if (token != null && jwtUtil.validateToken(token)) {
			final String email = jwtUtil.extractUsername(token);
			final String role = jwtUtil.extractRole(token);

			final var authorities = role != null
				? List.of(new SimpleGrantedAuthority("ROLE_" + role))
				: List.<SimpleGrantedAuthority>of();

			final var authenticationToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
			authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		final String path = request.getServletPath();
		return path.equals("/api/users/auth/login") || path.equals("/api/users/auth/logout");
	}
}
