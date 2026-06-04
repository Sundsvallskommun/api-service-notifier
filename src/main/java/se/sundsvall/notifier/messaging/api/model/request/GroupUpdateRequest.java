package se.sundsvall.notifier.messaging.api.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Builder;

@Builder(setterPrefix = "with")
public record GroupUpdateRequest(
	@NotBlank String name,
	@NotBlank String description,
	@NotNull Set<Long> employees) {
}
