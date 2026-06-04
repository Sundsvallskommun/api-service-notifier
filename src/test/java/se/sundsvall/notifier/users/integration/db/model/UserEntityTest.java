package se.sundsvall.notifier.users.integration.db.model;

import org.junit.jupiter.api.Test;
import se.sundsvall.notifier.users.integration.db.model.enums.Role;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class UserEntityTest {

	@Test
	void testBean() {
		assertThat(UserEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testBuildMethod() {
		final var id = 1L;
		final var email = "email";
		final var phoneNumber = "phoneNumber";
		final var municipalityId = "municipalityId";
		final var status = Status.valueOf("ACTIVE");
		final var role = Role.valueOf("USER");

		final var userEntity = UserEntity.create()
			.withId(id)
			.withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityId(municipalityId)
			.withStatus(status)
			.withRole(role);

		assertThat(userEntity.getId()).isEqualTo(id);
		assertThat(userEntity.getEmail()).isEqualTo(email);
		assertThat(userEntity.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(userEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(userEntity.getStatus()).isEqualTo(status);
		assertThat(userEntity.getRole()).isEqualTo(role);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(UserEntity.create()).hasAllNullFieldsOrPropertiesExcept("role");
		assertThat(new UserEntity()).hasAllNullFieldsOrPropertiesExcept("role");
	}
}
