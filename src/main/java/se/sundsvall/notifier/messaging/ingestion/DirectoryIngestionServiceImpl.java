package se.sundsvall.notifier.messaging.ingestion;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * JdbcTemplate-backed implementation of {@link DirectoryIngestionService}. Batch upserts are kept on
 * {@code JdbcTemplate} for throughput; the SQL lives here because {@code messaging} owns these tables.
 */
@Service
class DirectoryIngestionServiceImpl implements DirectoryIngestionService {

	private static final Logger LOG = LoggerFactory.getLogger(DirectoryIngestionServiceImpl.class);

	private static final String ORGANIZATION_UPSERT = """
		INSERT INTO organization (company_id, org_id, org_name, parent_org_id, tree_level)
		VALUES (?, ?, ?, ?, ?)
		ON DUPLICATE KEY UPDATE
		org_name = VALUES(org_name),
		    parent_org_id = VALUES(parent_org_id),
		    tree_level = VALUES(tree_level)
		""";

	private static final String EMPLOYEE_UPSERT = """
		INSERT INTO employee (person_id, first_name, last_name, work_mobile, work_phone, work_title, org_id, email, manager_id, manager_code, active_employee)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
		ON DUPLICATE KEY UPDATE
		    first_name      = VALUES(first_name),
		    last_name       = VALUES(last_name),
		    work_mobile     = VALUES(work_mobile),
		    work_phone      = VALUES(work_phone),
		    work_title      = VALUES(work_title),
		    email           = VALUES(email),
		    manager_id      = VALUES(manager_id),
		    manager_code    = VALUES(manager_code),
		    active_employee = VALUES(active_employee),
		    updated_at      = CURRENT_TIMESTAMP
		""";

	private static final String DEACTIVATE_STALE = """
		UPDATE employee
		SET active_employee = false
		WHERE active_employee = true
		  AND (updated_at IS NULL OR updated_at < ?)
		""";

	// Synthetic fallback organization for employees whose org_id is missing/unknown at import time.
	// PARENT_ID="13" must already exist as an org_id (top-level node from the organization import) or
	// the fk_org_parent FK insert fails — keep these aligned with the source directory's org tree.
	private static final String UNKNOWN_ORG_COMPANY_ID = "1";
	private static final String UNKNOWN_ORG_ID = "UNKNOWN";
	private static final String UNKNOWN_ORG_NAME = "Övriga personer";
	private static final String UNKNOWN_ORG_PARENT_ID = "13";
	private static final int UNKNOWN_ORG_TREE_LEVEL = 2;

	private final JdbcTemplate jdbcTemplate;

	DirectoryIngestionServiceImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void upsertOrganizations(List<OrganizationRecord> organizations) {
		if (organizations.isEmpty()) {
			return;
		}
		final var batch = organizations.stream()
			.map(o -> new Object[] {
				o.companyId(), o.orgId(), o.orgName(), o.parentId(), o.treeLevel()
			})
			.toList();
		jdbcTemplate.batchUpdate(ORGANIZATION_UPSERT, batch);
		LOG.info("[ORG] upsert complete. Rows sent to DB: {}", batch.size());
	}

	@Override
	public void ensureUnknownOrganization() {
		final var unknownOrganization = new ArrayList<Object[]>(1);
		unknownOrganization.add(new Object[] {
			UNKNOWN_ORG_COMPANY_ID, UNKNOWN_ORG_ID, UNKNOWN_ORG_NAME, UNKNOWN_ORG_PARENT_ID, UNKNOWN_ORG_TREE_LEVEL
		});
		jdbcTemplate.batchUpdate(ORGANIZATION_UPSERT, unknownOrganization);
		LOG.info("[ORG] ensured UNKNOWN fallback organization");
	}

	@Override
	public Instant beginEmployeeImport() {
		final var startedAt = jdbcTemplate.queryForObject("SELECT CURRENT_TIMESTAMP()", Timestamp.class);
		return startedAt.toInstant();
	}

	@Override
	public void upsertEmployees(List<EmployeeRecord> employees) {
		if (employees.isEmpty()) {
			return;
		}
		final var knownOrgIds = existingOrgIds(employees);
		final var batch = employees.stream()
			.map(e -> new Object[] {
				e.personId(), e.firstName(), e.lastName(), e.workMobile(), e.workPhone(), e.workTitle(),
				resolveOrgId(e.orgId(), knownOrgIds), e.email(), e.managerId(), e.managerCode(), true
			})
			.toList();
		jdbcTemplate.batchUpdate(EMPLOYEE_UPSERT, batch);
		LOG.info("[EMP] upsert complete. Rows sent to DB: {}", batch.size());
	}

	@Override
	public int deactivateEmployeesNotSeenSince(Instant importStartedAt) {
		final var deactivated = jdbcTemplate.update(DEACTIVATE_STALE, Timestamp.from(importStartedAt));
		LOG.info("[EMP] non updated employees set to inactive: {}", deactivated);
		return deactivated;
	}

	private Set<String> existingOrgIds(List<EmployeeRecord> employees) {
		final var orgIds = employees.stream()
			.map(EmployeeRecord::orgId)
			.filter(orgId -> orgId != null && !orgId.isBlank())
			.distinct()
			.toList();
		if (orgIds.isEmpty()) {
			return Set.of();
		}
		final var inSql = "SELECT org_id FROM organization WHERE org_id IN ("
			+ orgIds.stream().map(orgId -> "?").collect(Collectors.joining(","))
			+ ")";
		return new HashSet<>(jdbcTemplate.queryForList(inSql, String.class, orgIds.toArray()));
	}

	private String resolveOrgId(String orgId, Set<String> knownOrgIds) {
		if (orgId != null && !orgId.isBlank() && !knownOrgIds.contains(orgId)) {
			LOG.warn("[EMP] org_id '{}' not found, setting to UNKNOWN", orgId);
			return UNKNOWN_ORG_ID;
		}
		return orgId;
	}
}
