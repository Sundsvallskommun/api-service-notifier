package se.sundsvall.notifier.users.service;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.notifier.security.JwtUtil;
import se.sundsvall.notifier.users.api.model.JwtResponse;
import se.sundsvall.notifier.users.api.model.LoginRequest;
import se.sundsvall.notifier.users.integration.db.UserRepository;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
	@Mock
	private UserRepository userRepositoryMock;
	@Mock
	private PasswordEncoder passwordEncoderMock;
	@Mock
	private JwtUtil jwtServiceMock;
	@InjectMocks
	private AuthenticationService authenticationService;

	@Test
	void login() {
		final var loginRequest = LoginRequest.create()
			.withPassword("password")
			.withEmail("test@email.se");
		final var token = "token";
		final var hashedPassword = "hashedPassword";
		final UserEntity userEntity = UserEntity.create()
			.withEmail("test@email.se")
			.withPassword(hashedPassword);
		when(userRepositoryMock.findByEmail(loginRequest.getEmail()))
			.thenReturn(Optional.of(userEntity));
		when(passwordEncoderMock.matches("password", hashedPassword))
			.thenReturn(true);
		when(jwtServiceMock.generateToken(loginRequest.getEmail(), "USER"))
			.thenReturn(token);

		JwtResponse response = authenticationService.login(loginRequest);
		assertThat(response).isNotNull();
		assertThat(response.getToken()).isEqualTo(token);
	}

	@Test
	void login_whenAccountSuspended_shouldThrowForbidden() {
		final var loginRequest = LoginRequest.create()
			.withPassword("password")
			.withEmail("test@email.se");
		final var hashedPassword = "hashedPassword";
		final var userEntity = UserEntity.create()
			.withEmail("test@email.se")
			.withPassword(hashedPassword)
			.withStatus(Status.SUSPENDED);

		when(userRepositoryMock.findByEmail(loginRequest.getEmail()))
			.thenReturn(Optional.of(userEntity));
		when(passwordEncoderMock.matches("password", hashedPassword))
			.thenReturn(true);

		final var exception = assertThrows(Throwable.class,
			() -> authenticationService.login(loginRequest));

		assertThat(exception)
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Account suspended");
	}

	@Test
	void login_whenAccountInactive_shouldThrowForbidden() {
		final var loginRequest = LoginRequest.create()
			.withPassword("password")
			.withEmail("test@email.se");
		final var hashedPassword = "hashedPassword";
		final var userEntity = UserEntity.create()
			.withEmail("test@email.se")
			.withPassword(hashedPassword)
			.withStatus(Status.INACTIVE);

		when(userRepositoryMock.findByEmail(loginRequest.getEmail()))
			.thenReturn(Optional.of(userEntity));
		when(passwordEncoderMock.matches("password", hashedPassword))
			.thenReturn(true);

		final var exception = assertThrows(Throwable.class,
			() -> authenticationService.login(loginRequest));

		assertThat(exception)
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Account inactive");
	}
}
