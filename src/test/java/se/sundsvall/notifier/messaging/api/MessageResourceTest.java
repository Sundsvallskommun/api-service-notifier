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
import se.sundsvall.notifier.messaging.api.model.request.MessageRequest;
import se.sundsvall.notifier.messaging.api.model.request.MessageRequestWithoutRecipient;
import se.sundsvall.notifier.messaging.api.model.request.MessageType;
import se.sundsvall.notifier.messaging.api.model.response.MessageResponse;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
@Sql(
	scripts = {
		"/db/script/truncate.sql", "/db/script/testdata.sql"
	},
	executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MessageResourceTest {

	@Autowired
	private WebTestClient webTestClient;

	private static final String BASE_PATH = "/api/notifier/messages";

	@Test
	void createMessage_ok() {
		var request = MessageRequest.builder()
			.withContent("content")
			.withSender("sender@sundsvall.se")
			.withTitle("title")
			.withRecipientEmployeeIds(Set.of(1L, 2L, 3L))
			.withMessageType(MessageType.SMS)
			.build();

		webTestClient.post()
			.uri(BASE_PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();
	}

	@Test
	void createMessage_invalidBody() {
		var invalid = MessageRequest.builder()
			.withSender("no-email")
			.build();

		webTestClient.post()
			.uri(BASE_PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(invalid)
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Test
	void sendMessageToAll_ok() {
		var request = MessageRequestWithoutRecipient.builder()
			.withContent("content")
			.withSender("sender@sundsvall.se")
			.withTitle("title")
			.withMessageType(MessageType.SMS)
			.build();

		webTestClient.post()
			.uri(BASE_PATH + "/all")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();
	}

	@Test
	void getMessageById_ok() {
		var sender = "test@sundsvall.se";
		Long messageId = 1L;
		final var response = webTestClient.get()
			.uri(BASE_PATH + "/{messageId}/{sender}", messageId, sender)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(MessageResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response).hasSize(1);
		assertThat(response).extracting(MessageResponse::sender).contains("test@sundsvall.se");
	}

	@Test
	void getMessageById_invalidSender() {
		var sender = "no-email";
		Long messageId = 1L;
		webTestClient.get()
			.uri(BASE_PATH + "/{messageId}/{sender}", messageId, sender)
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Test
	void getMessagesForSender_ok() {
		final var response = webTestClient.get()
			.uri(BASE_PATH + "?sender=test@sundsvall.se")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(MessageResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response).hasSize(1);
		assertThat(response)
			.extracting(MessageResponse::title)
			.contains("Test title");
	}

	@Test
	void getMessagesForSender_invalidSender() {
		webTestClient.get()
			.uri(BASE_PATH + "?sender=invalid")
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Test
	void deleteMessage_ok() {
		webTestClient.delete()
			.uri(BASE_PATH + "/1")
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		webTestClient.get()
			.uri(BASE_PATH + "?sender=test@sundsvall.se")
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(MessageResponse.class)
			.hasSize(0);
	}

	@Test
	void getRecipients_ok() {
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder
				.path(BASE_PATH + "/message" + "/3" + "/recipients")
				.queryParam("page", 0)
				.queryParam("size", 5)
				.build(2))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.content.length()").isEqualTo(1)
			.jsonPath("$.totalElements").isEqualTo(1)
			.jsonPath("$.content[0].firstName").isEqualTo("Test")
			.jsonPath("$.content[0].lastName").isEqualTo("Three")
			.jsonPath("$.content[0].deliveryStatus").isEqualTo("FAILED");
	}

}
