package se.sundsvall.notifier.users.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class UserRequestTest {

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

		final var userRequest = UserRequest.create()
			.withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityName(municipalityName)
			.withStatus(status);

		assertThat(userRequest.getEmail()).isEqualTo(email);
		assertThat(userRequest.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(userRequest.getMunicipalityName()).isEqualTo(municipalityName);
		assertThat(userRequest.getStatus()).isEqualTo(status);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(UserResponse.create()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testPasswordRoleAndSetters() {
		final var userRequest = UserRequest.create()
			.withEmail("a@b.se")
			.withPassword("secret")
			.withRole("ADMIN");

		assertThat(userRequest.getPassword()).isEqualTo("secret");
		assertThat(userRequest.getRole()).isEqualTo("ADMIN");

		userRequest.setEmail("c@d.se");
		userRequest.setPassword("changed");
		assertThat(userRequest.getEmail()).isEqualTo("c@d.se");
		assertThat(userRequest.getPassword()).isEqualTo("changed");
	}

	@Test
	void testEqualsAndHashCode() {
		final var one = UserRequest.create().withEmail("a@b.se").withPassword("p")
			.withPhoneNumber("070").withMunicipalityName("Sundsvall").withStatus("ACTIVE").withRole("USER");
		final var same = UserRequest.create().withEmail("a@b.se").withPassword("p")
			.withPhoneNumber("070").withMunicipalityName("Sundsvall").withStatus("ACTIVE").withRole("USER");
		final var different = UserRequest.create().withEmail("other@b.se");

		assertThat(one)
			.isEqualTo(one)
			.isEqualTo(same)
			.isNotEqualTo(different)
			.isNotEqualTo(null)
			.isNotEqualTo("a string");
		assertThat(one).hasSameHashCodeAs(same);
	}
}
