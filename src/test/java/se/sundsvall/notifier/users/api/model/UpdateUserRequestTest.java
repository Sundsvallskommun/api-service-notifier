package se.sundsvall.notifier.users.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class UpdateUserRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(UserEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testBuildMethod() {
		final var phoneNumber = "phoneNumber";
		final var municipalityName = "municipalityName";
		final var status = "ACTIVE";

		final var updateUserRequest = UpdateUserRequest.create()
			.withPhoneNumber(phoneNumber)
			.withMunicipalityName(municipalityName)
			.withStatus(status);

		assertThat(updateUserRequest.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(updateUserRequest.getMunicipalityName()).isEqualTo(municipalityName);
		assertThat(updateUserRequest.getStatus()).isEqualTo(status);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(UserResponse.create()).hasAllNullFieldsOrProperties();
	}
}
