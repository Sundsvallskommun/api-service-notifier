package se.sundsvall.notifier.csvimport.service;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.notifier.messaging.ingestion.DirectoryIngestionService;
import se.sundsvall.notifier.messaging.ingestion.EmployeeRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeImportServiceTest {

	@TempDir
	Path tempDir;

	@Mock
	DirectoryIngestionService ingestionService;

	EmployeeImportService importService;

	@BeforeEach
	void setup() throws Exception {
		importService = new EmployeeImportService(ingestionService);
		final var field = EmployeeImportService.class.getDeclaredField("batchSize");
		field.setAccessible(true);
		field.setInt(importService, 10);
	}

	@Test
	void importEmployeePassesParsedRows() throws Exception {
		// Arrange
		final var empCsv = tempDir.resolve("emp.csv");
		Files.writeString(empCsv, """
			PersonId;Givenname;Lastname;WorkMobile;WorkPhone;Title;OrgId;PrimaryEMailAddress;ManagerId;ManagerCode
			10;förnamn;efternamn;;;Lärare;NoOrg;eva@test.com;;
			""", Charset.forName("Windows-1252"));
		when(ingestionService.beginEmployeeImport()).thenReturn(Instant.now());

		// Act
		importService.importEmployee(empCsv);

		// Assert — rows are parsed and cleansed but org remapping is the ingestion module's job,
		// so the raw orgId ("NoOrg") is what's handed off here.
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<List<EmployeeRecord>> captor = (ArgumentCaptor<List<EmployeeRecord>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
		verify(ingestionService).upsertEmployees(captor.capture());

		final var batch = captor.getValue();
		assertThat(batch).hasSize(1);
		final var row = batch.getFirst();
		assertThat(row.personId()).isEqualTo("10");
		assertThat(row.firstName()).isEqualTo("förnamn");
		assertThat(row.lastName()).isEqualTo("efternamn");
		assertThat(row.workTitle()).isEqualTo("Lärare");
		assertThat(row.orgId()).isEqualTo("NoOrg");
		assertThat(row.email()).isEqualTo("eva@test.com");

		verify(ingestionService).beginEmployeeImport();
		verify(ingestionService).deactivateEmployeesNotSeenSince(any());
	}

	@Test
	void importEmployee_throwsException() {
		// Arrange
		final var missing = tempDir.resolve("missing.csv");

		// Act & Assert
		assertThatThrownBy(() -> importService.importEmployee(missing))
			.isInstanceOf(RuntimeException.class)
			.hasMessageStartingWith("Error Importing organization from:");
	}
}
