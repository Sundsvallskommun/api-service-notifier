package se.sundsvall.notifier.users.integration.db.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum Status {
	ACTIVE,
	INACTIVE,
	SUSPENDED;
}
