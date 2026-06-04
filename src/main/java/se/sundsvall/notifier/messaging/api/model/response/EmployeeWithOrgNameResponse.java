package se.sundsvall.notifier.messaging.api.model.response;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record EmployeeWithOrgNameResponse(
	Long id,
	String personId,
	String orgId,
	String firstName,
	String lastName,
	String email,
	String workMobile,
	String workPhone,
	String workTitle,
	String orgName) {}
