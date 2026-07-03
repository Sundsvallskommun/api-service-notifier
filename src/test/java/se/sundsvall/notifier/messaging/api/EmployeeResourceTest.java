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
import se.sundsvall.notifier.messaging.api.model.response.EmployeeManagerResponse;
import se.sundsvall.notifier.messaging.api.model.response.EmployeeWithOrgNameResponse;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
@Sql(
	scripts = {
		"/db/script/truncate.sql", "/db/script/testdata.sql"
	},
	executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class EmployeeResourceTest {

	@Autowired
	private WebTestClient webTestClient;

	private static final String BASE_PATH = "/api/notifier/employees";

	@Test
	void getEmployeesByOrgId_ok_returnsEmployeesInOrg() {
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/ORG-1")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(EmployeeWithOrgNameResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response).hasSize(1);
		assertThat(response)
			.extracting(EmployeeWithOrgNameResponse::personId)
			.containsExactly("p1");
	}

	@Test
	void getAll_ok_returnsAllEmployees() {
		final var response = webTestClient.get()
			.uri(BASE_PATH)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(EmployeeWithOrgNameResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response).hasSize(6);
		assertThat(response)
			.extracting(EmployeeWithOrgNameResponse::personId)
			.containsExactlyInAnyOrder("p1", "p2", "p3", "p10", "p11", "p12");
	}

	@Test
	void getEmployeesByOrgIds_ok_returnsMatchingEmployees() {
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/orgIds?orgIds=ORG-1&orgIds=ORG-2")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(EmployeeWithOrgNameResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response).hasSize(2);
		assertThat(response)
			.extracting(EmployeeWithOrgNameResponse::personId)
			.containsExactlyInAnyOrder("p1", "p2");
	}

	@Test
	void getEmployeesWithSearch_ok_returnsMatchingEmployees() {
		webTestClient.get()
			.uri(BASE_PATH + "/search?search=Test&page=0&size=10")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.content").isArray()
			.jsonPath("$.content.length()").isEqualTo(3)
			.jsonPath("$.totalElements").isEqualTo(3)
			.jsonPath("$.content[?(@.personId == 'p1')]").exists()
			.jsonPath("$.content[?(@.personId == 'p2')]").exists()
			.jsonPath("$.content[?(@.personId == 'p3')]").exists();
	}

	@Test
	void getEmployeesWithSearch_noMatch_returnsEmptyPage() {
		webTestClient.get()
			.uri(BASE_PATH + "/search?search=finnsinte&page=0&size=10")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.content").isArray()
			.jsonPath("$.content.length()").isEqualTo(0)
			.jsonPath("$.totalElements").isEqualTo(0);
	}

	@Test
	void getManagers_ok_returnsAllEmployeesWithManagerCode() {
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/managers")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(EmployeeManagerResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response).hasSize(3);
		assertThat(response)
			.extracting(EmployeeManagerResponse::managerCode)
			.containsExactlyInAnyOrder("A_", "B_", "C_");
	}

	@Test
	void getEmployeesForItProd_ok() {
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/org-group/it-prod").exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(EmployeeWithOrgNameResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull()
			.isNotEmpty()
			.hasSize(3);
		assertThat(response).extracting(EmployeeWithOrgNameResponse::firstName)
			.containsExactlyInAnyOrder("Alice", "Bob", "Charlie");
	}
}
