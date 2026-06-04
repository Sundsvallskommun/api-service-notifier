package se.sundsvall.notifier.messaging.api.model.response;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import se.sundsvall.notifier.messaging.api.model.request.MessageType;

@Builder(setterPrefix = "with")
public record MessageWithRecipientsResponse(
	Long id,
	String title,
	String content,
	String sender,
	MessageType messageType,
	LocalDateTime createdAt,
	Set<MessageRecipientResponse> recipients) {
}
