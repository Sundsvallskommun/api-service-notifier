package se.sundsvall.notifier.messaging.integration.smssender;

import lombok.Builder;
import se.sundsvall.notifier.messaging.api.model.request.Priority;

@Builder(setterPrefix = "with")
public record SmsDto(
	String sender,
	String mobileNumber,
	String message,
	Priority priority) {
}
