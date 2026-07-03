package se.sundsvall.notifier.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

/**
 * Single security filter chain for the merged service. Validates the HS256 JWT (from
 * {@code Authorization: Bearer} or a {@code token} cookie) for everything under {@code /api/**}:
 * <ul>
 * <li>{@code /api/users/auth/**} — open (login / logout, the issuance path)</li>
 * <li>{@code /api/users/**} — requires {@code ROLE_ADMIN} (user administration)</li>
 * <li>everything else under {@code /api/**} (i.e. {@code /api/notifier/**}) — any valid token</li>
 * </ul>
 * The chain is disabled under the {@code junit} / {@code it} test profiles; the
 * {@link PasswordEncoder} bean is always available.
 */
@Configuration
@EnableWebSecurity
@ExcludeFromJacocoGeneratedCoverageReport
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Profile("!junit & !it")
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.securityMatcher("/api/**")
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/users/auth/**").permitAll()
				.requestMatchers("/api/users/**").hasRole("ADMIN")
				.anyRequest().authenticated())
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
