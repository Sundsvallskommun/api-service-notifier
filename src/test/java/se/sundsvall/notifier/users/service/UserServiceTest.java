package se.sundsvall.notifier.users.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.sundsvall.dept44.problem.*;
import se.sundsvall.notifier.users.api.model.UpdateUserRequest;
import se.sundsvall.notifier.users.api.model.UserRequest;
import se.sundsvall.notifier.users.api.model.UserResponse;
import se.sundsvall.notifier.users.integration.db.UserRepository;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;
import se.sundsvall.notifier.users.integration.db.model.enums.Role;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;
import se.sundsvall.notifier.users.service.Mapper.UserMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepositoryMock;
	@Mock
	private UserMapper userMapperMock;
	@Mock
	private PasswordEncoder passwordEncoderMock;

	@InjectMocks
	private UserService userService;

	@Test
	void getUserByEmail() {
		// Arrange
		final var email = "Test123@mail.com";
		final var id = 1L;
		final var userEntity = UserEntity.create().withId(id).withEmail(email);
		final var expectedUser = new UserResponse();

		when(userRepositoryMock.findByEmail(email)).thenReturn(Optional.of(userEntity));
		when(userMapperMock.toUserResponse(userEntity)).thenReturn(expectedUser);

		// Act
		final var result = userService.getUserByEmail(email);

		// Assert
		assertThat(result).isSameAs(expectedUser);
		verify(userRepositoryMock).findByEmail(email);
		verify(userMapperMock).toUserResponse(userEntity);

	}

	@Test
	void getUserByPartyId() {
		// Arrange
		final var id = 1L;
		final var userEntity = UserEntity.create().withId(id);
		final var expectedUser = new UserResponse();

		when(userRepositoryMock.findById(id)).thenReturn(Optional.of(userEntity));
		when(userMapperMock.toUserResponse(userEntity)).thenReturn(expectedUser);

		// Act
		final var result = userService.getUserById(id);

		// Assert
		assertThat(result).isSameAs(expectedUser);
		verify(userRepositoryMock).findById(id);
		verify(userMapperMock).toUserResponse(userEntity);

	}

	@Test
	void createUser() {
		// Arrange
		final var email = "Test@testmail.com";
		final var phoneNumber = "0701740669";
		final var municipalityId = "2281";
		final var status = "ACTIVE";

		// Build request and expected entities/responses
		var userRequest = UserRequest.create()
			.withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityName("Sundsvall")
			.withStatus(status);

		var userEntity = UserEntity.create()
			.withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityId(municipalityId)
			.withStatus(Status.valueOf(status));

		var userResponse = UserResponse.create()
			.withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityName("Sundsvall")
			.withStatus(status);

		when(userRepositoryMock.findByEmail(email)).thenReturn(Optional.empty());
		when(userRepositoryMock.save(userEntity)).thenReturn(userEntity);
		when(userMapperMock.toUserEntity(eq(userRequest), anyString())).thenReturn(userEntity);
		when(userMapperMock.toUserResponse(userEntity)).thenReturn(userResponse);
		when(passwordEncoderMock.encode(userRequest.getPassword())).thenReturn("hashedPassword");
		// Act
		var created = userService.createUser(userRequest);

		// Assert
		// Verify we saved exactly that entity instance
		verify(userRepositoryMock).save(userEntity);

		assertThat(created).isNotNull();
		assertThat(created).isEqualTo(userResponse);
	}

	@Test
	void updateUserById() {
		// Arrange
		final var id = 1L;
		final var email = "Test@testmail.se";
		final var phoneNumber = "0701740619";
		final var municipalityId = "2281";
		final var status = "ACTIVE";
		final var userRequestMock = UpdateUserRequest.create()
			.withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityName("Sundsvall")
			.withStatus(status);
		final var userEntity = UserEntity.create().withId(id).withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityId(municipalityId)
			.withStatus(Status.valueOf(status));
		final var userResponseMock = UserResponse.create().withId(id).withEmail(email)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityName("Sundsvall")
			.withStatus(status);
		// Mock
		when(userRepositoryMock.findById(id)).thenReturn(Optional.of(userEntity));
		when(userRepositoryMock.save(userEntity)).thenReturn(userEntity);
		when(userMapperMock.toUserResponse(userEntity)).thenReturn(userResponseMock);
		when(userMapperMock.resolveMunicipalityId("Sundsvall")).thenReturn(municipalityId);

		// Act
		final var updatedUser = userService.updateUserById(userRequestMock, id);

		// Verify/Assert
		verify(userRepositoryMock).save(same(userEntity));
		assertThat(updatedUser).isNotNull();
		assertThat(updatedUser).isEqualTo(userResponseMock);
	}

	@Test
	void updateUserByPartyId() {
		// Arrange
		final var id = 1L;
		final var phoneNumber = "0701740619";
		final var municipalityId = "2281";
		final var status = "ACTIVE";
		final var userRequestMock = UpdateUserRequest.create()
			.withPhoneNumber(phoneNumber)
			.withMunicipalityName("Sundsvall")
			.withStatus(status);
		final var userEntity = UserEntity.create().withId(id)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityId(municipalityId)
			.withStatus(Status.valueOf(status));
		final var userResponseMock = UserResponse.create().withId(id)
			.withPhoneNumber(phoneNumber)
			.withMunicipalityName("Sundsvall")
			.withStatus(status);
		// Mock
		when(userRepositoryMock.findById(id)).thenReturn(Optional.of(userEntity));
		when(userRepositoryMock.save(userEntity)).thenReturn(userEntity);
		when(userMapperMock.toUserResponse(userEntity)).thenReturn(userResponseMock);
		when(userMapperMock.resolveMunicipalityId("Sundsvall")).thenReturn(municipalityId);

		// Act
		final var updatedUser = userService.updateUserById(userRequestMock, id);

		// Verify/Assert
		verify(userRepositoryMock).save(same(userEntity));
		assertThat(updatedUser).isNotNull();
		assertThat(updatedUser).isEqualTo(userResponseMock);
	}

	@Test
	void deleteUserByEmail() {

		// Arrange
		final var email = "Test@testmail.se";
		when(userRepositoryMock.findByEmail(email)).thenReturn(Optional.of(UserEntity.create().withEmail(email).withRole(Role.USER)));

		// Act
		userService.deleteUserByEmail(email);

		// Verify/Assert
		verify(userRepositoryMock).findByEmail(email);
		verify(userRepositoryMock).deleteByEmail(email);

	}

	@Test
	void deleteUserByPersonalNumber() {

		// Arrange
		final var personalNumber = "198001011234";
		when(userRepositoryMock.findByEmail(personalNumber)).thenReturn(Optional.of(UserEntity.create().withEmail(personalNumber).withRole(Role.USER)));

		// Act
		userService.deleteUserByEmail(personalNumber);

		// Verify/Assert
		verify(userRepositoryMock).findByEmail(personalNumber);
		verify(userRepositoryMock).deleteByEmail(personalNumber);

	}

	@Test
	void deleteUserById() {

		// Arrange
		final var id = 1L;
		when(userRepositoryMock.findById(id)).thenReturn(Optional.of(UserEntity.create().withId(id).withRole(Role.USER)));

		// Act
		userService.deleteUserById(id);

		// Verify/Assert
		verify(userRepositoryMock).findById(id);
		verify(userRepositoryMock).deleteById(id);

	}

	@Test
	void createUserAlreadyExists() {
		// Arrange
		final var email = "Test@testmail.se";
		final var userRequest = new UserRequest();
		userRequest.setEmail(email);

		// Mock
		when(userRepositoryMock.findByEmail(email)).thenReturn(Optional.of(new UserEntity()));

		// Act & Assert
		final var exception = assertThrows(Throwable.class, () -> userService.createUser(userRequest));

		assertThat((exception))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("user already exists");

		verify(userRepositoryMock).findByEmail(email);
		verify(userRepositoryMock, never()).save(any());
		verifyNoMoreInteractions(userRepositoryMock, userMapperMock);
	}

	@Test
	void getUserByEmailNotFound() {
		// Arrange
		final var email = "Test@testmail.com";

		when(userRepositoryMock.findByEmail(email)).thenReturn(Optional.empty());

		// Act
		final var exception = assertThrows(Throwable.class, () -> userService.getUserByEmail(email));
		// Assert
		assertThat(exception)
			.isInstanceOf(Problem.class)
			.hasMessageContaining("user " + email + " was not found");

		verify(userRepositoryMock).findByEmail(email);
		verify(userRepositoryMock, never()).getReferenceById(any());
		verify(userMapperMock, never()).toUserResponse(any());
		verifyNoMoreInteractions(userRepositoryMock, userMapperMock);
	}

	@Test
	void updateUserNotFound() {
		// Arrange
		final var id = 99L;
		final var request = UpdateUserRequest.create();

		// Mock
		when(userRepositoryMock.findById(id)).thenReturn(Optional.empty());
		final var problem = assertThrows(Throwable.class, () -> userService.updateUserById(request, id));

		// Assert
		assertThat(problem)
			.isNotNull()
			.hasMessage("Not Found: user " + id + " was not found");
	}

	@Test
	void getAllUsers() {
		final var userEntity1 = UserEntity.create().withId(1L).withEmail("user1@test.se");
		final var userEntity2 = UserEntity.create().withId(2L).withEmail("user2@test.se");
		final var userResponse1 = UserResponse.create().withId(1L).withEmail("user1@test.se");
		final var userResponse2 = UserResponse.create().withId(2L).withEmail("user2@test.se");

		when(userRepositoryMock.findAllByRole(Role.USER)).thenReturn(List.of(userEntity1, userEntity2));
		when(userMapperMock.toUserResponse(userEntity1)).thenReturn(userResponse1);
		when(userMapperMock.toUserResponse(userEntity2)).thenReturn(userResponse2);

		final var result = userService.getAllUsers();

		assertThat(result)
			.hasSize(2)
			.containsExactly(userResponse1, userResponse2);
		verify(userRepositoryMock).findAllByRole(Role.USER);
		verify(userMapperMock).toUserResponse(userEntity1);
		verify(userMapperMock).toUserResponse(userEntity2);
	}

}
