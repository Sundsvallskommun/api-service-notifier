package se.sundsvall.notifier.users.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class JwtResponseTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(JwtResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void constructorSetterAndNullToken() {
		final var jwtResponse = new JwtResponse("token");
		assertThat(jwtResponse.getToken()).isEqualTo("token");

		jwtResponse.setToken("newToken");
		assertThat(jwtResponse.getToken()).isEqualTo("newToken");

		assertThat(new JwtResponse().getToken()).isNull();
	}

	@Test
	void equalsHashCodeAndToString() {
		final var response = new JwtResponse("token");
		final var equal = new JwtResponse("token");

		assertThat(response)
			.isEqualTo(response)
			.isEqualTo(equal)
			.hasSameHashCodeAs(equal)
			.isNotEqualTo(null)
			.isNotEqualTo("not a JwtResponse")
			.isNotEqualTo(new JwtResponse("other"));
		assertThat(response).hasToString("JwtResponse{token='token'}");
	}
}
