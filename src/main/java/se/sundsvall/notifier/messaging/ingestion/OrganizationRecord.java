package se.sundsvall.notifier.messaging.ingestion;

/**
 * A single organization row handed from the {@code csvimport} module to the {@code messaging}
 * module for upsert. Fields are pre-cleansed strings (blank / "NULL" already mapped to null by the
 * caller); {@code treeLevel} is passed as a string and coerced by the database.
 */
public record OrganizationRecord(
	String companyId,
	String orgId,
	String orgName,
	String parentId,
	String treeLevel) {
}
