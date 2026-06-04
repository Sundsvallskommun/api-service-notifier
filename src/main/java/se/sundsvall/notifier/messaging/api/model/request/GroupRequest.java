package se.sundsvall.notifier.messaging.api.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Builder;

@Builder(setterPrefix = "with")
public record GroupRequest(
	@NotBlank String name,
	@NotBlank String description,
	@NotBlank String creatorId,
	@NotNull Set<Long> employees) {
}
