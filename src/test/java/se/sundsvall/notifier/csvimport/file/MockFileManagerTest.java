package se.sundsvall.notifier.csvimport.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MockFileManagerTest {

	@TempDir
	Path tempDir;

	private MockFileManager mockFileManager;

	@BeforeEach
	void setUp() {
		mockFileManager = new MockFileManager();
		ReflectionTestUtils.setField(mockFileManager, "fileSourceDir", tempDir.resolve("temp"));
	}

	@Test
	void downloadFile_copiesFileFromTempToIncoming() throws IOException {
		var tempSource = tempDir.resolve("temp");
		var incomingDir = tempDir.resolve("incoming");
		Files.createDirectories(tempSource);

		var content = "col1;col2\nval1;val2";
		Files.writeString(tempSource.resolve("OrgExport.csv"), content);

		mockFileManager.downloadFile(incomingDir, "OrgExport.csv");

		var copied = incomingDir.resolve("OrgExport.csv");
		assertThat(copied).exists();
		assertThat(copied).hasContent(content);
	}

	@Test
	void downloadFile_whenSourceFileMissing_doesNotThrow() {
		var incomingDir = tempDir.resolve("incoming");

		// Source file does not exist under fileSourceDir -> Files.copy throws, which the mock swallows.
		assertThatCode(() -> mockFileManager.downloadFile(incomingDir, "missing.csv"))
			.doesNotThrowAnyException();
	}
}
