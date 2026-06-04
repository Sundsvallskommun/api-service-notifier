package se.sundsvall.notifier.messaging.api.model.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder(setterPrefix = "with")
public record MessageRecipientResponse(
	Long employeeId,
	String firstName,
	String lastName,
	String orgId,
	String orgName,
	String workTitle,
	String deliveryStatus,
	LocalDateTime receivedAt) {
}
