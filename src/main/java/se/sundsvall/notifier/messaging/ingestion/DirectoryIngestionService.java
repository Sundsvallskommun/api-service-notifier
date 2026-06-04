package se.sundsvall.notifier.messaging.ingestion;

import java.time.Instant;
import java.util.List;

/**
 * Ingestion API owned by the {@code messaging} module for populating its {@code organization} and
 * {@code employee} tables. The {@code csvimport} module parses CSV files and feeds rows through this
 * interface instead of writing the tables directly — keeping a single owner for the schema and
 * making the cross-module dependency explicit (and Modulith-verified).
 */
public interface DirectoryIngestionService {

	/**
	 * Upserts a batch of organizations (insert or update on {@code org_id}).
	 */
	void upsertOrganizations(List<OrganizationRecord> organizations);

	/**
	 * Ensures the synthetic {@code UNKNOWN} fallback organization exists — the parent for employees
	 * whose source {@code orgId} does not resolve. Idempotent; call once per organization import.
	 */
	void ensureUnknownOrganization();

	/**
	 * Records the database-side start time of an employee import, used afterwards by
	 * {@link #deactivateEmployeesNotSeenSince(Instant)} to detect leavers. Call before upserting.
	 */
	Instant beginEmployeeImport();

	/**
	 * Upserts a batch of employees. Any {@code orgId} not present in the {@code organization} table
	 * is remapped to {@code "UNKNOWN"} before persisting.
	 */
	void upsertEmployees(List<EmployeeRecord> employees);

	/**
	 * Deactivates every currently-active employee not touched since {@code importStartedAt} (i.e.
	 * absent from the latest import). Returns the number of rows deactivated.
	 */
	int deactivateEmployeesNotSeenSince(Instant importStartedAt);
}
