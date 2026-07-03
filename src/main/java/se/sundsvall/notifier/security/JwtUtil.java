package se.sundsvall.notifier.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Shared JWT utility for the merged service.
 *
 * <p>
 * Issuance ({@link #generateToken}) is used by the {@code users} module on login; validation /
 * claim extraction is used by {@link JwtAuthenticationFilter} to authenticate every request under
 * {@code /api/**}. Co-locating issuance and validation in one process means the HS256 secret lives
 * in a single env var (no cross-service duplication). If auth is ever split back out, the issuance
 * path moves with the {@code users} module.
 */
@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private Long expiration;

	public String generateToken(String email, String role) {
		final Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);
		claims.put("email", email);
		return Jwts.builder()
			.claims(claims)
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expiration))
			.signWith(getSignKey())
			.compact();
	}

	public String extractUsername(String token) {
		return extractClaim(token, claims -> claims.get("email", String.class));
	}

	public String extractRole(String token) {
		return extractClaim(token, claims -> claims.get("role", String.class));
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSignKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private SecretKey getSignKey() {
		final byte[] keyBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public boolean validateToken(String token) {
		try {
			return !extractExpiration(token).before(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Overload kept for the users module's login/admin flows: also asserts the token's {@code email}
	 * claim matches the expected user. The request-authentication path uses {@link #validateToken(String)}.
	 */
	public boolean validateToken(String token, String email) {
		try {
			final var extracted = extractUsername(token);
			return extracted != null && extracted.equals(email) && validateToken(token);
		} catch (Exception e) {
			return false;
		}
	}
}
