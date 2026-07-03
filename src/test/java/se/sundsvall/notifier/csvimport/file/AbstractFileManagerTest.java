package se.sundsvall.notifier.csvimport.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractFileManagerTest {

	@TempDir
	Path tempDir;

	private AbstractFileManager fileManager;

	@BeforeEach
	void setUp() {
		fileManager = new AbstractFileManager() {
			@Override
			public void downloadFile(Path dir, String fileName) {
				// Tested in Sftp and MockFileManagerTest
			}
		};
	}

	@Test
	void testMoveOrganizationFiles() throws IOException {
		var sourceDir = tempDir.resolve("incoming");
		var targetDir = tempDir.resolve("processed");
		Files.createDirectories(sourceDir);
		var content = "string";

		var orgCsv = sourceDir.resolve("OrgExport.csv");
		Files.writeString(orgCsv, content);

		fileManager.moveFile(orgCsv, targetDir);

		var moved = targetDir.resolve("OrgExport.csv");
		assertThat(moved).exists();
		assertThat(orgCsv).doesNotExist();
		assertThat(moved).hasContent(content);
	}

	@Test
	void testMoveEmployeeFiles() throws IOException {
		var sourceDir = tempDir.resolve("incoming");
		var targetDir = tempDir.resolve("processed");
		Files.createDirectories(sourceDir);
		Files.createDirectories(targetDir);
		var content = "string";

		var empCsv = sourceDir.resolve("EmpExport.csv");
		Files.writeString(empCsv, content);

		fileManager.moveFile(empCsv, targetDir);

		var moved = targetDir.resolve("EmpExport.csv");
		assertThat(moved).exists();
		assertThat(empCsv).doesNotExist();
		assertThat(moved).hasContent(content);
	}

	@Test
	void testMoveFile_whenTargetDirIsAFile_shouldThrowIllegalStateException() throws IOException {
		var incomingDir = tempDir.resolve("incoming");
		Files.createDirectories(incomingDir);

		var filePath = incomingDir.resolve("OrgExport.csv");
		Files.writeString(filePath, "string");

		var processedDir = tempDir.resolve("processed");
		Files.writeString(processedDir, "file");

		assertThatThrownBy(() -> fileManager.moveFile(filePath, processedDir))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void testDeletePreviouslyProcessedFile() throws IOException {
		var processed = tempDir.resolve("processed.csv");
		Files.writeString(processed, "string");
		assertThat(processed).exists();

		fileManager.deletePreviouslyProcessedFile(processed);

		assertThat(processed).doesNotExist();
	}

	@Test
	void deleteProcessedFileWhenFileDoesNotExistTest() throws IOException {
		var dir = tempDir.resolve("dir");
		Files.createDirectory(dir);
		Files.writeString(dir.resolve("file.txt"), "test");

		assertThatThrownBy(() -> fileManager.deletePreviouslyProcessedFile(dir))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Failed to delete file");
	}

	@Test
	void verifyReadableWhenNoFileTest() {
		var missingFile = tempDir.resolve("missing.csv");

		assertThatThrownBy(() -> fileManager.verifyReadable(missingFile, "ORG"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageStartingWith("File does not exist:");
	}

	@Test
	void verifyReadable_whenFileExists_doesNotThrow() throws IOException {
		var file = tempDir.resolve("OrgExport.csv");
		Files.writeString(file, "string");

		assertThatCode(() -> fileManager.verifyReadable(file, "ORG")).doesNotThrowAnyException();
	}

	@Test
	void verifyReadable_throwsException() throws IOException {
		var directory = tempDir.resolve("directory");
		Files.createDirectory(directory);

		assertThatThrownBy(() -> fileManager.verifyReadable(directory, "ORG"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageStartingWith("Failed reading file:");
	}
}
