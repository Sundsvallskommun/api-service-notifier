package se.sundsvall.notifier.messaging.ingestion;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("junit")
@Sql("/db/script/truncate.sql")
class DirectoryIngestionServiceTest {

	@Autowired
	private DirectoryIngestionService ingestionService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void upsertsOrganizationsAndEmployeesRemappingUnknownOrgAndDeactivatingLeavers() {
		// Capture the import start before any writes — leavers are detected relative to this.
		final var importStartedAt = ingestionService.beginEmployeeImport();
		assertThat(importStartedAt).isNotNull();

		// Org tree first (incl. id 13, the hardcoded parent of the UNKNOWN fallback), then UNKNOWN —
		// mirrors the production order where the CSV's root org exists before the fallback is ensured.
		ingestionService.upsertOrganizations(List.of(
			new OrganizationRecord("1", "13", "Root", null, "0"),
			new OrganizationRecord("1", "100", "Org 100", "13", "2")));
		ingestionService.ensureUnknownOrganization();

		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM organization WHERE org_id = 'UNKNOWN'", Integer.class)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject("SELECT org_name FROM organization WHERE org_id = '100'", String.class)).isEqualTo("Org 100");

		// A pre-existing employee that this import does not touch (stale updated_at).
		jdbcTemplate.update("INSERT INTO employee (person_id, org_id, work_title, active_employee, updated_at) "
			+ "VALUES ('stale', '100', 'Old', true, '2000-01-01 00:00:00')");

		ingestionService.upsertEmployees(List.of(
			new EmployeeRecord("p1", "First", "Last", "070", "08", "Dev", "100", "p1@x.se", null, null),
			new EmployeeRecord("p2", "Foo", "Bar", null, null, "Ops", "DOES_NOT_EXIST", "p2@x.se", null, null)));

		// Known org kept; unresolvable org remapped to UNKNOWN.
		assertThat(jdbcTemplate.queryForObject("SELECT org_id FROM employee WHERE person_id = 'p1'", String.class)).isEqualTo("100");
		assertThat(jdbcTemplate.queryForObject("SELECT org_id FROM employee WHERE person_id = 'p2'", String.class)).isEqualTo("UNKNOWN");

		// Only the untouched 'stale' row is deactivated; the freshly imported ones survive.
		final int deactivated = ingestionService.deactivateEmployeesNotSeenSince(importStartedAt);
		assertThat(deactivated).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject("SELECT active_employee FROM employee WHERE person_id = 'stale'", Boolean.class)).isFalse();
		assertThat(jdbcTemplate.queryForObject("SELECT active_employee FROM employee WHERE person_id = 'p1'", Boolean.class)).isTrue();
	}

	@Test
	void emptyBatchesAreNoOps() {
		ingestionService.upsertOrganizations(List.of());
		ingestionService.upsertEmployees(List.of());

		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM organization", Integer.class)).isZero();
		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employee", Integer.class)).isZero();
	}
}
