package se.sundsvall.notifier.messaging.integration.teamssender;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record TeamsSenderDTO(
	String recipient,
	String message) {}
