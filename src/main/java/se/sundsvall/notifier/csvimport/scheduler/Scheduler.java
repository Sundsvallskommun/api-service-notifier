package se.sundsvall.notifier.csvimport.scheduler;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.notifier.csvimport.file.FileManager;
import se.sundsvall.notifier.csvimport.service.EmployeeImportService;
import se.sundsvall.notifier.csvimport.service.OrganizationImportService;

@EnableScheduling
@Configuration
@ConditionalOnProperty(name = "import.enabled", havingValue = "true")
public class Scheduler {

	@Value("${import.incoming-dir}")
	private Path incomingDir;
	@Value("${import.processed-dir}")
	private Path processedDir;

	@Value("${import.org-file-name}")
	private String orgFileName;
	@Value("${import.emp-file-name}")
	private String empFileName;

	private final EmployeeImportService employeeImportService;
	private final OrganizationImportService organizationImportService;
	private final FileManager fileManager;

	public Scheduler(EmployeeImportService employeeImportService, OrganizationImportService organizationImportService,
		FileManager fileManager) {
		this.employeeImportService = employeeImportService;
		this.organizationImportService = organizationImportService;
		this.fileManager = fileManager;
	}

	/**
	 * Single scheduled entry point. Organizations are imported before employees because employee
	 * org-resolution reads the {@code organization} table — a separate, racing emp job could otherwise
	 * remap real employees to the UNKNOWN org. One ShedLock makes the two steps atomic; if the org step
	 * throws (including a failed SFTP download), the emp step is skipped and the cycle retries next run.
	 */
	@Dept44Scheduled(
		cron = "${scheduler.directory-import.cron}",
		name = "${scheduler.directory-import.name}",
		lockAtMostFor = "${scheduler.directory-import.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.directory-import.maximum-execution-time}")
	public void importDirectoryJob() {
		importOrganizationsJob();
		importEmployeesJob();
	}

	public void importOrganizationsJob() {

		var orgCsv = incomingDir.resolve(orgFileName);
		var oldOrgCsv = processedDir.resolve(orgFileName);
		try {

			fileManager.downloadFile(incomingDir, orgFileName);
			organizationImportService.importOrganizations(orgCsv);
			fileManager.deletePreviouslyProcessedFile(oldOrgCsv);
			fileManager.moveFile(orgCsv, processedDir);

		} catch (Exception e) {
			throw new RuntimeException("[ORG] Import failed", e);
		}
	}

	public void importEmployeesJob() {

		var empCsv = incomingDir.resolve(empFileName);
		var oldEmpFile = processedDir.resolve(empFileName);
		try {

			fileManager.downloadFile(incomingDir, empFileName);
			employeeImportService.importEmployee(empCsv);
			fileManager.deletePreviouslyProcessedFile(oldEmpFile);
			fileManager.moveFile(empCsv, processedDir);

		} catch (Exception e) {
			throw new RuntimeException("[EMP] Import failed", e);
		}
	}
}
