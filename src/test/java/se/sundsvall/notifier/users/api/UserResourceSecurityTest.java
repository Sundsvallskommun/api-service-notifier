package se.sundsvall.notifier.users.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import se.sundsvall.notifier.security.JwtAuthenticationFilter;
import se.sundsvall.notifier.security.JwtUtil;
import se.sundsvall.notifier.security.SecurityConfig;
import se.sundsvall.notifier.users.service.UserService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the HTTP-layer admin gate on {@code /api/users/**} — specifically that the delete
 * endpoints reject non-admins. Runs the real {@link SecurityConfig} chain (this context is under no
 * profile, so it is <em>not</em> disabled the way it is under {@code junit}/{@code it}) over a
 * minimal web context, with {@link JwtUtil} mocked to mint the caller role. This is the regression
 * guard for {@code SecurityConfig}: change the {@code /api/users/**} matcher and this fails.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
	SecurityConfig.class, JwtAuthenticationFilter.class, UserResource.class
})
class UserResourceSecurityTest {

	private static final String TOKEN = "test-token";

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockitoBean
	private UserService userServiceMock;

	@MockitoBean
	private JwtUtil jwtUtilMock;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(springSecurity())
			.build();
	}

	private void authenticateAs(final String role) {
		when(jwtUtilMock.validateToken(TOKEN)).thenReturn(true);
		when(jwtUtilMock.extractUsername(TOKEN)).thenReturn("caller@test.se");
		when(jwtUtilMock.extractRole(TOKEN)).thenReturn(role);
	}

	@Test
	void deleteById_noToken_isUnauthorized() throws Exception {
		mockMvc.perform(delete("/api/users/ids/{id}", 1))
			.andExpect(status().isUnauthorized());
		verifyNoInteractions(userServiceMock);
	}

	@Test
	void deleteById_userRole_isForbidden() throws Exception {
		authenticateAs("USER");
		mockMvc.perform(delete("/api/users/ids/{id}", 1).header("Authorization", "Bearer " + TOKEN))
			.andExpect(status().isForbidden());
		verifyNoInteractions(userServiceMock);
	}

	@Test
	void deleteById_adminRole_isNoContent() throws Exception {
		authenticateAs("ADMIN");
		mockMvc.perform(delete("/api/users/ids/{id}", 1).header("Authorization", "Bearer " + TOKEN))
			.andExpect(status().isNoContent());
		verify(userServiceMock).deleteUserById(1L);
	}

	@Test
	void deleteByEmail_userRole_isForbidden() throws Exception {
		authenticateAs("USER");
		mockMvc.perform(delete("/api/users/emails/{email}", "victim@test.se").header("Authorization", "Bearer " + TOKEN))
			.andExpect(status().isForbidden());
		verifyNoInteractions(userServiceMock);
	}

	@Test
	void deleteByEmail_adminRole_isNoContent() throws Exception {
		authenticateAs("ADMIN");
		mockMvc.perform(delete("/api/users/emails/{email}", "victim@test.se").header("Authorization", "Bearer " + TOKEN))
			.andExpect(status().isNoContent());
		verify(userServiceMock).deleteUserByEmail("victim@test.se");
	}
}
