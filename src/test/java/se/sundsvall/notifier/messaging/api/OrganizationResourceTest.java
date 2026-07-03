package se.sundsvall.notifier.messaging.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.notifier.Application;
import se.sundsvall.notifier.messaging.api.model.response.OrganizationResponse;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
@Sql(
	scripts = {
		"/db/script/truncate.sql", "/db/script/testdata.sql"
	},
	executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrganizationResourceTest {

	@Autowired
	private WebTestClient webTestClient;

	private static final String BASE_PATH = "/api/notifier/organization";

	@Test
	void getOne_ok_returnsOrganization() {
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/ORG-1")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody(OrganizationResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.orgId()).isEqualTo("ORG-1");
		assertThat(response.name()).isEqualTo("Org 1");
	}

	@Test
	void getOne_notFound_returns404() {
		webTestClient.get()
			.uri(BASE_PATH + "/ORG-999")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void getAll_ok_returnsAllOrganizations() {
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/organizations")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(OrganizationResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response).hasSize(6);
		assertThat(response)
			.extracting(OrganizationResponse::orgId)
			.containsExactlyInAnyOrder("ORG-1", "ORG-2", "ORG-3", "11212", "11215", "500398");
	}

	@Test
	void getWithList_ok_returnsMatchingOrganizations() {
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/ids?orgId=ORG-1&orgId=ORG-2")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(OrganizationResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response).hasSize(2);
		assertThat(response)
			.extracting(OrganizationResponse::orgId)
			.containsExactlyInAnyOrder("ORG-1", "ORG-2");
	}

	@Test
	void getOrgChildrenAndDescendants_ok_returnsDescendants() {
		// ORG-1 är förälder till ORG-2 och ORG-3
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/ORG-1/children/descendants")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(OrganizationResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response)
			.extracting(OrganizationResponse::orgId)
			.containsExactlyInAnyOrder("ORG-1", "ORG-2", "ORG-3");
	}

	@Test
	void getOrgAndChildren_ok_returnsChildren() {
		// ORG-1 är förälder till ORG-2 och ORG-3
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/ORG-1/children")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(OrganizationResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response)
			.extracting(OrganizationResponse::orgId)
			.containsExactlyInAnyOrder("ORG-2", "ORG-3");
	}
}
