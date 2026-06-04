package se.sundsvall.notifier.messaging.api.model.response;

import lombok.Builder;

@Builder(setterPrefix = "with", toBuilder = true)
public record OrganizationResponse(
	String companyId,
	String parentOrgId,
	String orgId,
	String name,
	int treeLevel) {}
