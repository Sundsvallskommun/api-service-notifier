package se.sundsvall.notifier.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

/**
 * Enables {@code @PreAuthorize} method security — the defense-in-depth backstop behind the URL
 * matcher in {@link SecurityConfig}. Gated to the same {@code !junit & !it} profiles as the filter
 * chain: enforcement is disabled under the test profiles (so AppTests hit endpoints without a
 * principal), and the property is instead proven by the dedicated security tests
 * ({@code UserResourceSecurityTest} for the URL layer, {@code UserServiceMethodSecurityTest} for the
 * {@code @PreAuthorize} layer), which enable the chain / method security explicitly.
 */
@Configuration
@EnableMethodSecurity
@Profile("!junit & !it")
@ExcludeFromJacocoGeneratedCoverageReport
public class MethodSecurityConfig {
}
