package se.sundsvall.notifier.messaging.ingestion;

/**
 * A single employee row handed from the {@code csvimport} module to the {@code messaging} module
 * for upsert. Fields are pre-cleansed strings; {@code orgId} is the raw source value — remapping an
 * unresolvable {@code orgId} to {@code "UNKNOWN"} is the messaging module's responsibility (it owns
 * the {@code organization} table the value is validated against).
 */
public record EmployeeRecord(
	String personId,
	String firstName,
	String lastName,
	String workMobile,
	String workPhone,
	String workTitle,
	String orgId,
	String email,
	String managerId,
	String managerCode) {
}
