package se.sundsvall.notifier.users.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class LoginRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(LoginRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testBuildMethod() {
		final var request = LoginRequest.create()
			.withEmail("test@test.se")
			.withPassword("secret");

		assertThat(request.getEmail()).isEqualTo("test@test.se");
		assertThat(request.getPassword()).isEqualTo("secret");
	}

	@Test
	void equalsHashCodeAndToString() {
		final var request = LoginRequest.create().withEmail("a@test.se").withPassword("secret");
		final var equal = LoginRequest.create().withEmail("a@test.se").withPassword("secret");

		assertThat(request)
			.isEqualTo(request)
			.isEqualTo(equal)
			.hasSameHashCodeAs(equal)
			.isNotEqualTo(null)
			.isNotEqualTo("not a LoginRequest")
			.isNotEqualTo(LoginRequest.create().withEmail("b@test.se").withPassword("secret"))
			.isNotEqualTo(LoginRequest.create().withEmail("a@test.se").withPassword("other"));
		assertThat(request).hasToString("LoginRequest{email='a@test.se', password='secret'}");
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(LoginRequest.create()).hasAllNullFieldsOrProperties();
	}
}
