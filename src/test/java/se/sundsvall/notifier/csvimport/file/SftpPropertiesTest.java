package se.sundsvall.notifier.csvimport.file;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.notifier.Application;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class SftpPropertiesTest {

	@Autowired
	private SftpProperties sftpProperties;

	@Test
	void testProperties() {
		assertThat(sftpProperties.username()).isEqualTo("test");
		assertThat(sftpProperties.password()).isEqualTo("test");
		assertThat(sftpProperties.remoteHost()).isEqualTo("sftp://localhost/test");
		assertThat(sftpProperties.connectTimeout()).isEqualTo(Duration.ofSeconds(15));
		assertThat(sftpProperties.sessionTimeout()).isEqualTo(Duration.ofSeconds(15));
	}
}
