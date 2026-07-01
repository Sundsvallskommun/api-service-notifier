package se.sundsvall.notifier.messaging.api.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Builder;

@Builder(setterPrefix = "with")
public record MessageRequest(
	@Schema(description = "Titel", example = "Viktigt meddelande") @NotBlank String title,

	@Schema(description = "Innehållet på meddelandet", example = "Det finns inget kaffe!") @NotBlank @Size(min = 1, max = 160) String content,

	@Schema(description = "Vilken användare som skickat meddelandet", example = "kalle.kula@sundsvall.se") @NotBlank String sender,

	@Schema(description = "Lista på mottagare id", example = "[1, 2, 3]") @NotEmpty(message = "recipientEmployeeIds must contain at least one id") Set<Long> recipientEmployeeIds,

	@Schema(description = "Kan var SMS, TEAMS eller TEAMS_AND_SMS", example = "SMS") @NotNull MessageType messageType) {
}
