package se.sundsvall.notifier.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

	private static final String TEST_SECRET = "dGVzdFNlY3JldEtleUZvclRlc3RpbmdQdXJwb3Nlc09ubHkxMg==";

	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();
		ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
		ReflectionTestUtils.setField(jwtUtil, "expiration", 3_600_000L);
	}

	private String generateToken(String email, String role, long expirationMs) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", email);
		claims.put("role", role);
		return Jwts.builder()
			.claims(claims)
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + expirationMs))
			.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET)))
			.compact();
	}

	@Test
	void extractUsername_ok() {
		var token = generateToken("test@example.com", "USER", 60_000);
		assertThat(jwtUtil.extractUsername(token)).isEqualTo("test@example.com");
	}

	@Test
	void extractRole_ok() {
		var token = generateToken("test@example.com", "ADMIN", 60_000);
		assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
	}

	@Test
	void extractExpiration_isInFuture() {
		var token = generateToken("test@example.com", "USER", 60_000);
		assertThat(jwtUtil.extractExpiration(token)).isAfter(new Date());
	}

	@Test
	void validateToken_valid_returnsTrue() {
		var token = generateToken("test@example.com", "USER", 60_000);
		assertThat(jwtUtil.validateToken(token)).isTrue();
	}

	@Test
	void validateToken_expired_returnsFalse() {
		var token = generateToken("test@example.com", "USER", -1_000);
		assertThat(jwtUtil.validateToken(token)).isFalse();
	}

	@Test
	void validateToken_invalid_returnsFalse() {
		assertThat(jwtUtil.validateToken("not.a.valid.token")).isFalse();
	}

	@Test
	void generateToken_roundTrips() {
		var token = jwtUtil.generateToken("issued@example.com", "ADMIN");
		assertThat(jwtUtil.extractUsername(token)).isEqualTo("issued@example.com");
		assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
		assertThat(jwtUtil.validateToken(token)).isTrue();
	}

	@Test
	void validateTokenWithEmail_matching_returnsTrue() {
		var token = jwtUtil.generateToken("issued@example.com", "USER");
		assertThat(jwtUtil.validateToken(token, "issued@example.com")).isTrue();
	}

	@Test
	void validateTokenWithEmail_mismatch_returnsFalse() {
		var token = jwtUtil.generateToken("issued@example.com", "USER");
		assertThat(jwtUtil.validateToken(token, "someone-else@example.com")).isFalse();
	}

	@Test
	void validateTokenWithEmail_invalid_returnsFalse() {
		assertThat(jwtUtil.validateToken("not.a.valid.token", "issued@example.com")).isFalse();
	}
}
