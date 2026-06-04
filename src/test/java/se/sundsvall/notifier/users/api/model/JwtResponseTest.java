package se.sundsvall.notifier.users.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class JwtResponseTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(UserEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testGettersAndSetters() {
		final var token = "token";

		final var jwtResponse = new JwtResponse(token);
		assertThat(jwtResponse.getToken()).isEqualTo(token);

	}

	@Test
	void testNullToken() {
		final var jwtResponse = new JwtResponse();
		assertThat(jwtResponse.getToken()).isNull();
	}

	@Test
	void constructorShouldSetToken() {
		String token = "token";

		JwtResponse jwtResponse = new JwtResponse(token);

		assertThat(jwtResponse.getToken()).isEqualTo(token);
	}

	@Test
	void setterShouldUpdateToken() {
		JwtResponse jwtResponse = new JwtResponse("oldToken");

		jwtResponse.setToken("newToken");

		assertThat(jwtResponse.getToken()).isEqualTo("newToken");
	}

	@Test
	void testHashCodeAndEquals() {
		final var r1 = new JwtResponse("token");
		final var r2 = new JwtResponse("token");

		assertThat(r1).isEqualTo(r2);
		assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
	}
}
