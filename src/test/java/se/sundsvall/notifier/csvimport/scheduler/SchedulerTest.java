package se.sundsvall.notifier.csvimport.scheduler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mockito;
import se.sundsvall.notifier.csvimport.file.FileManager;
import se.sundsvall.notifier.csvimport.service.EmployeeImportService;
import se.sundsvall.notifier.csvimport.service.OrganizationImportService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class SchedulerTest {

	@TempDir
	Path tempDir;

	@Test
	void importOrganizationsJobTest() throws Exception {
		// arrange
		EmployeeImportService employeeImportService = Mockito.mock(EmployeeImportService.class);
		OrganizationImportService organizationImportService = Mockito.mock(OrganizationImportService.class);
		FileManager fileManager = Mockito.mock(FileManager.class);

		Scheduler scheduler = new Scheduler(employeeImportService, organizationImportService, fileManager);

		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");

		Files.createDirectories(incomingDir);
		Files.createDirectories(processedDir);

		String orgCsv = "org.csv";

		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "orgFileName", orgCsv);

		Path expectedFile = incomingDir.resolve(orgCsv);
		Path expectedOldFile = processedDir.resolve(orgCsv);

		// act
		scheduler.importOrganizationsJob();

		verify(organizationImportService).importOrganizations(expectedFile);
		verify(fileManager).deletePreviouslyProcessedFile(expectedOldFile);
		verify(fileManager).moveFile(expectedFile, processedDir);
	}

	@Test
	void importEmployeesJobTest() throws Exception {
		// arrange
		EmployeeImportService employeeImportService = Mockito.mock(EmployeeImportService.class);
		OrganizationImportService organizationImportService = Mockito.mock(OrganizationImportService.class);
		FileManager fileManager = Mockito.mock(FileManager.class);

		Scheduler scheduler = new Scheduler(employeeImportService, organizationImportService, fileManager);

		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");

		Files.createDirectories(incomingDir);
		Files.createDirectories(processedDir);

		String empCsv = "emp.csv";

		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "empFileName", empCsv);

		Path expectedFile = incomingDir.resolve(empCsv);
		Path expectedOldFile = processedDir.resolve(empCsv);

		// act
		scheduler.importEmployeesJob();

		verify(employeeImportService).importEmployee(expectedFile);
		verify(fileManager).deletePreviouslyProcessedFile(expectedOldFile);
		verify(fileManager).moveFile(expectedFile, processedDir);
	}

	@Test
	void importEmployeeJob_throwsException() throws IOException {
		EmployeeImportService employeeImportService = Mockito.mock(EmployeeImportService.class);
		OrganizationImportService organizationImportService = Mockito.mock(OrganizationImportService.class);
		FileManager fileManager = Mockito.mock(FileManager.class);

		Scheduler scheduler = new Scheduler(employeeImportService, organizationImportService, fileManager);

		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");

		Files.createDirectories(incomingDir);
		Files.createDirectories(processedDir);

		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "empFileName", "emp.csv");

		doThrow(new RuntimeException("exception")).when(employeeImportService).importEmployee(any(Path.class));

		RuntimeException exception = assertThrows(RuntimeException.class, scheduler::importEmployeesJob);

		assertTrue(exception.getMessage().startsWith("[EMP] Import failed"));
	}

	@Test
	void importOrganizationJob_throwsException() throws IOException {
		EmployeeImportService employeeImportService = Mockito.mock(EmployeeImportService.class);
		OrganizationImportService organizationImportService = Mockito.mock(OrganizationImportService.class);
		FileManager fileManager = Mockito.mock(FileManager.class);

		Scheduler scheduler = new Scheduler(employeeImportService, organizationImportService, fileManager);

		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");

		Files.createDirectories(incomingDir);
		Files.createDirectories(processedDir);

		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "orgFileName", "org.csv");

		doThrow(new RuntimeException("exception")).when(organizationImportService).importOrganizations(any(Path.class));

		RuntimeException exception = assertThrows(RuntimeException.class, scheduler::importOrganizationsJob);

		assertTrue(exception.getMessage().startsWith("[ORG] Import failed"));
	}

	@Test
	void importDirectoryJob_importsOrganizationsBeforeEmployees() throws Exception {
		EmployeeImportService employeeImportService = Mockito.mock(EmployeeImportService.class);
		OrganizationImportService organizationImportService = Mockito.mock(OrganizationImportService.class);
		FileManager fileManager = Mockito.mock(FileManager.class);

		Scheduler scheduler = new Scheduler(employeeImportService, organizationImportService, fileManager);

		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");
		Files.createDirectories(incomingDir);
		Files.createDirectories(processedDir);

		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "orgFileName", "org.csv");
		setField(scheduler, "empFileName", "emp.csv");

		scheduler.importDirectoryJob();

		// Org must be imported before employees — employee org-resolution depends on a populated org table.
		final InOrder inOrder = inOrder(organizationImportService, employeeImportService);
		inOrder.verify(organizationImportService).importOrganizations(incomingDir.resolve("org.csv"));
		inOrder.verify(employeeImportService).importEmployee(incomingDir.resolve("emp.csv"));
	}

	@Test
	void importDirectoryJob_orgFailureSkipsEmployees() throws Exception {
		EmployeeImportService employeeImportService = Mockito.mock(EmployeeImportService.class);
		OrganizationImportService organizationImportService = Mockito.mock(OrganizationImportService.class);
		FileManager fileManager = Mockito.mock(FileManager.class);

		Scheduler scheduler = new Scheduler(employeeImportService, organizationImportService, fileManager);

		Path incomingDir = tempDir.resolve("incoming");
		Path processedDir = tempDir.resolve("processed");
		Files.createDirectories(incomingDir);
		Files.createDirectories(processedDir);

		setField(scheduler, "incomingDir", incomingDir);
		setField(scheduler, "processedDir", processedDir);
		setField(scheduler, "orgFileName", "org.csv");
		setField(scheduler, "empFileName", "emp.csv");

		doThrow(new RuntimeException("boom")).when(organizationImportService).importOrganizations(any(Path.class));

		// Org failure must abort the whole job so employees are never imported against stale/empty orgs.
		assertThrows(RuntimeException.class, scheduler::importDirectoryJob);
		verify(employeeImportService, never()).importEmployee(any(Path.class));
	}
}
