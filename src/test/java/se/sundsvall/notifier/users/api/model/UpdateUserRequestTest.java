package se.sundsvall.notifier.users.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class UpdateUserRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(UpdateUserRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testBuildMethod() {
		final var request = UpdateUserRequest.create()
			.withEmail("test@test.se")
			.withPhoneNumber("0701234567")
			.withMunicipalityName("Sundsvall")
			.withStatus("ACTIVE")
			.withRole("USER");

		assertThat(request.getEmail()).isEqualTo("test@test.se");
		assertThat(request.getPhoneNumber()).isEqualTo("0701234567");
		assertThat(request.getMunicipalityName()).isEqualTo("Sundsvall");
		assertThat(request.getStatus()).isEqualTo("ACTIVE");
		assertThat(request.getRole()).isEqualTo("USER");
	}

	@Test
	void equalsHashCodeAndToString() {
		final var request = UpdateUserRequest.create()
			.withEmail("a@test.se").withPhoneNumber("070").withMunicipalityName("Sundsvall").withStatus("ACTIVE").withRole("USER");
		final var equal = UpdateUserRequest.create()
			.withEmail("a@test.se").withPhoneNumber("070").withMunicipalityName("Sundsvall").withStatus("ACTIVE").withRole("USER");
		// same email but differing base field -> exercises the super.equals() branch
		final var differentBase = UpdateUserRequest.create()
			.withEmail("a@test.se").withPhoneNumber("999").withMunicipalityName("Sundsvall").withStatus("ACTIVE").withRole("USER");
		// same base fields but differing email -> exercises the email branch
		final var differentEmail = UpdateUserRequest.create()
			.withEmail("b@test.se").withPhoneNumber("070").withMunicipalityName("Sundsvall").withStatus("ACTIVE").withRole("USER");

		assertThat(request)
			.isEqualTo(request)
			.isEqualTo(equal)
			.hasSameHashCodeAs(equal)
			.isNotEqualTo(null)
			.isNotEqualTo("not an UpdateUserRequest")
			.isNotEqualTo(differentBase)
			.isNotEqualTo(differentEmail);
		assertThat(request).hasToString("UpdateUserRequest{email='a@test.se'}");
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(UpdateUserRequest.create()).hasAllNullFieldsOrProperties();
	}
}
