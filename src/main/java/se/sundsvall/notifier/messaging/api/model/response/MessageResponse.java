package se.sundsvall.notifier.messaging.api.model.response;

import java.time.LocalDateTime;
import lombok.Builder;
import se.sundsvall.notifier.messaging.api.model.request.MessageType;

@Builder(setterPrefix = "with")
public record MessageResponse(
	Long id,
	String title,
	String content,
	String sender,
	MessageType messageType,
	LocalDateTime createdAt) {
}
