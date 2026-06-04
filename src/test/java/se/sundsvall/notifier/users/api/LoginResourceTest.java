package se.sundsvall.notifier.users.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.notifier.Application;
import se.sundsvall.notifier.security.JwtUtil;
import se.sundsvall.notifier.users.api.model.JwtResponse;
import se.sundsvall.notifier.users.api.model.LoginRequest;
import se.sundsvall.notifier.users.service.AuthenticationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class LoginResourceTest {

	@MockitoBean
	private AuthenticationService authenticationServiceMock;
	@MockitoBean
	private JwtUtil jwtUtilMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void login() {
		final var jwtResponse = new JwtResponse("token");
		final var loginRequest = LoginRequest.create()
			.withEmail("Test@email.se")
			.withPassword("password");

		when(authenticationServiceMock.login(any(LoginRequest.class))).thenReturn(jwtResponse);
		final var response = webTestClient.post()
			.uri("/api/users/auth/login")
			.contentType(APPLICATION_JSON)
			.bodyValue(loginRequest)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().exists("Set-Cookie")
			.expectCookie().exists("token")
			.expectBody(String.class)
			.isEqualTo("Logged in!")
			.returnResult();

		final var setCookie = response.getResponseHeaders().getFirst("Set-Cookie");

		assertThat(setCookie).isNotNull();
		assertThat(setCookie).contains("token=");
		assertThat(setCookie).contains("HttpOnly");
		assertThat(setCookie).contains("Path=/");
		assertThat(setCookie).contains("SameSite=Strict");
		assertThat(setCookie).doesNotContain("SemeSite=Secure");
	}

	@Test
	void logoutTest() {
		final var response = webTestClient.post()
			.uri("/api/users/auth/logout")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().exists("Set-Cookie")
			.expectCookie().exists("token")
			.expectBody(String.class)
			.isEqualTo("Logged out")
			.returnResult();

		final var setCookie = response.getResponseHeaders().getFirst("Set-Cookie");

		assertThat(setCookie).isNotNull();
		assertThat(setCookie).contains("token=");
		assertThat(setCookie).contains("Max-Age=0");
		assertThat(setCookie).contains("HttpOnly");
		assertThat(setCookie).contains("Path=/");
		assertThat(setCookie).contains("SameSite=Strict");
		assertThat(setCookie).contains("Secure");
	}
}
