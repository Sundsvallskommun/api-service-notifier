package se.sundsvall.notifier.messaging.api.model.response;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record EmployeeManagerResponse(
	Long id,
	String personId,
	String orgId,
	String firstName,
	String lastName,
	String email,
	String workMobile,
	String workPhone,
	String workTitle,
	String managerCode) {
}
