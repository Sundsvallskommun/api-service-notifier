package se.sundsvall.notifier.messaging.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.notifier.messaging.api.model.request.MessageRequest;
import se.sundsvall.notifier.messaging.api.model.request.MessageRequestWithoutRecipient;
import se.sundsvall.notifier.messaging.api.model.response.MessageRecipientResponse;
import se.sundsvall.notifier.messaging.api.model.response.MessageResponse;
import se.sundsvall.notifier.messaging.service.MessageService;

@RestController
@RequestMapping(path = "/api/notifier/messages", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Message Resource")
@ApiResponse(
	responseCode = "400",
	description = "Bad Request",
	content = @Content(schema = @Schema(implementation = Problem.class)))
@ApiResponse(
	responseCode = "500",
	description = "Internal Server Error",
	content = @Content(schema = @Schema(implementation = Problem.class)))
public class MessageResource {

	private final MessageService messageService;

	public MessageResource(MessageService messageService) {
		this.messageService = messageService;
	}

	@Operation(description = "Create a new message")
	@ApiResponse(responseCode = "204", description = "No Content")
	@PostMapping
	public ResponseEntity<Void> sendMessage(@RequestBody @Valid MessageRequest message) {
		messageService.createMessage(message);
		return ResponseEntity.noContent().build();
	}

	@Operation(description = "Send message to all employees")
	@PostMapping("/all")
	@ApiResponse(responseCode = "200", description = "Successful Operation")
	public ResponseEntity<Void> sendMessageToAll(@RequestBody @Valid MessageRequestWithoutRecipient message) {
		messageService.sendMessageToAll(message);
		return ResponseEntity.noContent().build();
	}

	@Operation(description = "Get a single message from user")
	@GetMapping("/{messageId}/{sender}")
	@ApiResponse(responseCode = "200", description = "Successful Operation")
	@ApiResponse(responseCode = "404", description = "Not Found")
	public ResponseEntity<MessageResponse> getMessage(@PathVariable @Email String sender, @PathVariable Long messageId) {
		return ResponseEntity.ok(messageService.getMessageById(sender, messageId));
	}

	@Operation(description = "Get message from specific user")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@GetMapping
	public ResponseEntity<List<MessageResponse>> getMessages(@RequestParam("sender") @Email String sender) {

		var messageHistory = messageService.getMessages(sender);
		return ResponseEntity.ok(messageHistory);
	}

	// Denna endpoint är mer för clean up under utveckling
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
		messageService.deleteMessages(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/message/{messageId}/recipients")
	public ResponseEntity<Page<MessageRecipientResponse>> getRecipients(@PathVariable Long messageId, @ParameterObject Pageable pageable) {

		return ResponseEntity.ok(messageService.getRecipientsWithMessageId(messageId, pageable));
	}
}
