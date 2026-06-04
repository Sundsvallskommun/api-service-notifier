package se.sundsvall.notifier.messaging.api.model.response;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;

@Builder(setterPrefix = "with")
public record GroupResponse(
	Long id,
	String name,
	String description,
	String creatorId,
	Set<EmployeeWithOrgNameResponse> employees,
	LocalDateTime createdAt) {
}
