package se.sundsvall.notifier.users.service;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.sundsvall.notifier.users.integration.db.UserRepository;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;
import se.sundsvall.notifier.users.integration.db.model.enums.Role;
import se.sundsvall.notifier.users.service.mapper.UserMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the {@code @PreAuthorize("hasRole('ADMIN')")} backstop on {@link UserService} — the
 * defense-in-depth layer that is independent of the URL matcher in {@code SecurityConfig}. Uses a
 * minimal method-security context (no web, no DB) so the annotations are enforced by the real
 * advisor on the real {@code UserService} bean, with the caller identity supplied by
 * {@code @WithMockUser}.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserServiceMethodSecurityTest.TestConfig.class)
class UserServiceMethodSecurityTest {

	@Configuration
	@EnableMethodSecurity
	static class TestConfig {

		@Bean
		UserRepository userRepository() {
			return mock(UserRepository.class);
		}

		@Bean
		UserMapper userMapper() {
			return mock(UserMapper.class);
		}

		@Bean
		PasswordEncoder passwordEncoder() {
			return mock(PasswordEncoder.class);
		}

		@Bean
		UserService userService(final UserRepository userRepository, final UserMapper userMapper, final PasswordEncoder passwordEncoder) {
			return new UserService(userRepository, userMapper, passwordEncoder);
		}
	}

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepositoryMock;

	@BeforeEach
	void resetMocks() {
		reset(userRepositoryMock);
	}

	@Test
	@WithMockUser(roles = "USER")
	void nonAdminCannotDeleteUser() {
		// AccessDeniedException can only originate from the @PreAuthorize proxy — the method body
		// throws a dept44 Problem, never AccessDenied — so this proves the call was blocked before entry.
		assertThatThrownBy(() -> userService.deleteUserById(1L))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@WithAnonymousUser
	void anonymousCannotDeleteUser() {
		assertThatThrownBy(() -> userService.deleteUserById(1L))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@WithMockUser(roles = "USER")
	void nonAdminCannotDeleteByEmail() {
		assertThatThrownBy(() -> userService.deleteUserByEmail("victim@test.se"))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanDeleteNonAdminUser() {
		final var target = UserEntity.create().withId(1L).withRole(Role.USER);
		when(userRepositoryMock.findById(1L)).thenReturn(Optional.of(target));

		userService.deleteUserById(1L);

		verify(userRepositoryMock).findById(1L);
		verify(userRepositoryMock).deleteById(1L);
	}
}
