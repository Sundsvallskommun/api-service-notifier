package se.sundsvall.notifier.users.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class UserResponseTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(UserEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testBuildMethod() {
		final var email = "email";
		final var phoneNumber = "phoneNumber";
		final var municipalityName = "municipalityName";
		final var status = "ACTIVE";

		final var userResponse = UserResponse.create()
			.withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityName(municipalityName)
			.withStatus(status);

		assertThat(userResponse.getEmail()).isEqualTo(email);
		assertThat(userResponse.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(userResponse.getMunicipalityName()).isEqualTo(municipalityName);
		assertThat(userResponse.getStatus()).isEqualTo(status);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(UserResponse.create()).hasAllNullFieldsOrProperties();
	}
}
