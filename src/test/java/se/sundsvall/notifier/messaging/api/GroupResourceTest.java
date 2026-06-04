package se.sundsvall.notifier.messaging.api;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.notifier.Application;
import se.sundsvall.notifier.messaging.api.model.request.GroupRequest;
import se.sundsvall.notifier.messaging.api.model.request.GroupUpdateRequest;
import se.sundsvall.notifier.messaging.api.model.response.GroupResponse;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
@Sql(
	scripts = {
		"/db/script/truncate.sql", "/db/script/testdata.sql"
	},
	executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class GroupResourceTest {

	@Autowired
	private WebTestClient webTestClient;

	private static final String BASE_PATH = "/api/notifier/groups";

	@Test
	void getGroups_withoutCreatorId_ok_returnsAllGroups() {
		final var response = webTestClient.get()
			.uri(BASE_PATH)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(GroupResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response)
			.extracting(GroupResponse::name)
			.containsExactlyInAnyOrder("G1", "G2", "G3");
	}

	@Test
	void getGroups_withCreatorId_ok_returnsGroupsByCreator() {
		final var response = webTestClient.get()
			.uri(BASE_PATH + "?creatorId=creator-123")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(GroupResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response)
			.extracting(GroupResponse::name)
			.containsExactlyInAnyOrder("G1", "G2");
	}

	@Test
	void getGroupById_ok() {
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/1")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody(GroupResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("G1");
	}

	@Test
	void getGroupById_notFound_returns404() {
		webTestClient.get()
			.uri(BASE_PATH + "/999")
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void createGroup_ok_returns201_andLocationHeader() {
		var request = GroupRequest.builder()
			.withName("Team A")
			.withDescription("Beskrivning")
			.withCreatorId("creator-123")
			.withEmployees(Set.of(1L, 2L))
			.build();

		final var location = webTestClient.post()
			.uri(BASE_PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().exists("Location")
			.returnResult(Void.class)
			.getResponseHeaders()
			.getLocation();

		assertThat(location).isNotNull();
		assertThat(location.toString()).startsWith("/api/notifier/groups/");
	}

	@Test
	void createGroup_invalidBody_returns400() {
		var invalid = GroupRequest.builder()
			.withName("")
			.withDescription("")
			.withCreatorId("")
			.withEmployees(null)
			.build();

		webTestClient.post()
			.uri(BASE_PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(invalid)
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Test
	void updateGroup_ok_returnsUpdatedGroup() {
		var groupId = 1L;

		var request = GroupUpdateRequest.builder()
			.withName("New name")
			.withDescription("New desc")
			.withEmployees(Set.of(1L))
			.build();

		final var response = webTestClient.put()
			.uri(BASE_PATH + "/" + groupId)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(GroupResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(groupId);
		assertThat(response.name()).isEqualTo("New name");
		assertThat(response.description()).isEqualTo("New desc");
	}

	@Test
	void updateGroup_invalidBody_returns400() {
		var groupId = 1L;

		var invalid = GroupUpdateRequest.builder()
			.withName("")
			.withDescription("")
			.withEmployees(null)
			.build();

		webTestClient.put()
			.uri(BASE_PATH + "/" + groupId)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(invalid)
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Test
	void deleteGroup_ok_returns204() {
		webTestClient.delete()
			.uri(BASE_PATH + "/1")
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		webTestClient.get()
			.uri(BASE_PATH + "/1")
			.exchange()
			.expectStatus().isNotFound();
	}
}
