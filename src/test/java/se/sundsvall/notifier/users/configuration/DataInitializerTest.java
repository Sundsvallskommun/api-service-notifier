package se.sundsvall.notifier.users.configuration;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.notifier.users.integration.db.UserRepository;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;
import se.sundsvall.notifier.users.integration.db.model.enums.Role;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;
import se.sundsvall.notifier.users.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

	@InjectMocks
	private DataInitializer dataInitializer;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserService userService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(dataInitializer, "email", "test@example.se");
		ReflectionTestUtils.setField(dataInitializer, "password", "password");
		ReflectionTestUtils.setField(dataInitializer, "phoneNumber", "0701234567");
		ReflectionTestUtils.setField(dataInitializer, "municipalityId", "2281");
	}

	@Test
	void createUserWhenNotExists() {
		when(userRepository.findByEmail("test@example.se")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("password")).thenReturn("hashedpassword");

		dataInitializer.run();

		var captor = ArgumentCaptor.forClass(UserEntity.class);
		verify(userRepository).save(captor.capture());

		var savedUser = captor.getValue();
		assertThat(savedUser.getEmail()).isEqualTo("test@example.se");
		assertThat(savedUser.getMunicipalityId()).isEqualTo("2281");
		assertThat(savedUser.getPhoneNumber()).isEqualTo("0701234567");
		assertThat(savedUser.getStatus()).isEqualTo(Status.ACTIVE);
		assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
		assertThat(savedUser.getPassword()).isEqualTo("hashedpassword");
	}

	@Test
	void doNotSaveWhenUserAlreadyHasAdminRole() {
		var existingUser = UserEntity.create().withEmail("test@example.se").withRole(Role.ADMIN);
		when(userRepository.findByEmail("test@example.se")).thenReturn(Optional.of(existingUser));

		dataInitializer.run();

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateRoleToAdminWhenUserExistsWithWrongRole() {
		var existingUser = UserEntity.create().withEmail("test@example.se").withRole(Role.USER);
		when(userRepository.findByEmail("test@example.se")).thenReturn(Optional.of(existingUser));

		dataInitializer.run();

		verify(userRepository).save(argThat(user -> user.getRole() == Role.ADMIN));
	}

	@Test
	void hashPasswordBeforeSaving() {
		when(userRepository.findByEmail("test@example.se")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("password")).thenReturn("hashedPassword");

		dataInitializer.run();

		verify(passwordEncoder).encode("password");
		verify(userRepository).save(argThat(user -> user.getPassword().equals("hashedPassword")));
	}
}
