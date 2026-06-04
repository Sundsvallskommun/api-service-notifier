package se.sundsvall.notifier.users.service.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.notifier.users.api.model.UserRequest;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;
import se.sundsvall.notifier.users.service.Mapper.UserMapper;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

	@InjectMocks
	UserMapper userMapper;

	@Test
	void toUserResponse() {
		// Arrange
		final var email = "TestMail123@mail.se";
		final var phoneNumber = "99070121212";
		final var municipalityId = "2281";
		final var status = "ACTIVE";

		final var userEntity = UserEntity.create().withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityId(municipalityId)
			.withStatus(Status.valueOf(status));

		// Act
		final var result = userMapper.toUserResponse(userEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo(email);
		assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(result.getMunicipalityName()).isEqualTo("Sundsvall");
		assertThat(result.getStatus()).isEqualTo(status);
	}

	@Test
	void toUserEntity() {
		// Arrange
		final var email = "TestMail123@mail.se";
		final var phoneNumber = "99070121212";
		final var municipalityName = "Sundsvall";
		final var status = "ACTIVE";
		final String password = "password";

		final var userRequest = UserRequest.create().withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityName(municipalityName)
			.withStatus(status);
		// Act
		final var result = userMapper.toUserEntity(userRequest, password);
		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getEmail()).isEqualTo(email);
		assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(result.getMunicipalityId()).isEqualTo("2281");
		assertThat(result.getStatus()).isEqualTo(Status.valueOf(status));
		assertThat(result.getPassword()).isEqualTo(password);
	}

}
