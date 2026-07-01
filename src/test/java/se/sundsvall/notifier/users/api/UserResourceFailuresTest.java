package se.sundsvall.notifier.users.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.*;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.notifier.Application;
import se.sundsvall.notifier.security.JwtUtil;
import se.sundsvall.notifier.users.api.model.UpdateUserRequest;
import se.sundsvall.notifier.users.api.model.UserRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("it")
class UserResourceFailuresTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private JwtUtil jwtUtilMock;

	@BeforeEach
	public void setup() {
		webTestClient = webTestClient.mutate()
			.defaultHeader("Authorization", "Bearer test-token")
			.build();

		when(jwtUtilMock.validateToken(any(), any())).thenReturn(true);
		when(jwtUtilMock.extractUsername(any())).thenReturn("test@testmail.com");
	}

	@Test
	void saveUserWithBadRequest() {
		// Arrange
		final var userRequest = UserRequest.create()
			.withEmail("notamailtestcom")
			.withPhoneNumber("number0000000000")
			.withMunicipalityName("id2222")
			.withStatus("status");
		// Act
		final var response = webTestClient.post()
			.uri("/api/users")
			.contentType(APPLICATION_JSON)
			.bodyValue(userRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		Assertions.assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(
				tuple("email", "must be a valid Email-adress"),
				tuple("status", "must be one of: [ACTIVE, INACTIVE, SUSPENDED] (case-insensitive)"),
				tuple("phoneNumber", "must be a valid mobile number"),
				tuple("password", "must not be blank"),
				tuple("municipalityName", "must be a valid municipality name"));

	}

	@Test
	void getUserWithInvalidEmail() {
		// Arrange
		final String email = "kallekula";

		// Act
		final var response = webTestClient.get().uri("/api/users/emails/{email}", email)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(
				tuple("getUserByEmail.email", "must be a well-formed email address"));
	}

	@Test
	void updateUserWithBadRequest() {
		// Arrange
		final var id = 1L;
		final var userRequest = UpdateUserRequest.create()
			.withEmail("test@testmail.com")
			.withPhoneNumber("numberplate")
			.withMunicipalityName("municipalityId")
			.withStatus("status");

		// act
		final var response = webTestClient.patch().uri("/api/users/ids/{id}", id)
			.bodyValue(userRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(
				tuple("status", "must be one of: [ACTIVE, INACTIVE, SUSPENDED] (case-insensitive)"),
				tuple("phoneNumber", "must be a valid mobile number"),
				tuple("municipalityName", "must be a valid municipality name"),
				tuple("role", "must be one of: [ADMIN, USER] (case-insensitive)"));
	}
}
