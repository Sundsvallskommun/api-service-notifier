package se.sundsvall.notifier.csvimport.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.notifier.messaging.ingestion.DirectoryIngestionService;
import se.sundsvall.notifier.messaging.ingestion.OrganizationRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrganizationImportServiceTest {

	@TempDir
	Path tempDir;

	@Mock
	DirectoryIngestionService ingestionService;

	OrganizationImportService importService;

	@BeforeEach
	void setup() throws Exception {
		importService = new OrganizationImportService(ingestionService);
		final var field = OrganizationImportService.class.getDeclaredField("batchSize");
		field.setAccessible(true);
		field.setInt(importService, 10);
	}

	@Test
	void importOrganizations() throws Exception {
		// Arrange
		final Path orgCsv = tempDir.resolve("org.csv");
		Files.writeString(orgCsv, """
			CompanyId;OrgId;OrgName;ParentId;TreeLevel
			1;A;Org A;13;1
			""");

		// Act
		importService.importOrganizations(orgCsv);

		// Assert — the parsed row is handed off, and the UNKNOWN fallback is ensured separately
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<List<OrganizationRecord>> captor = (ArgumentCaptor<List<OrganizationRecord>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
		verify(ingestionService).upsertOrganizations(captor.capture());
		assertThat(captor.getValue()).hasSize(1);
		assertThat(captor.getValue().getFirst().orgId()).isEqualTo("A");
		verify(ingestionService).ensureUnknownOrganization();
	}

	@Test
	void importOrganization_throwsException() {
		// Arrange
		final Path missing = tempDir.resolve("missing.csv");

		// Act & Assert
		assertThatThrownBy(() -> importService.importOrganizations(missing))
			.isInstanceOf(RuntimeException.class)
			.hasMessageStartingWith("Error Importing organization from:");
	}
}
