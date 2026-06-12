package se.sundsvall.notifier.csvimport.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.sundsvall.notifier.csvimport.model.EmployeeDTO;
import se.sundsvall.notifier.csvimport.service.utility.ImportUtil;
import se.sundsvall.notifier.messaging.ingestion.DirectoryIngestionService;
import se.sundsvall.notifier.messaging.ingestion.EmployeeRecord;
import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

/**
 * Parses the employee CSV and hands batches to the messaging module's
 * {@link DirectoryIngestionService}. Unknown-org remapping and leaver deactivation are the
 * messaging module's responsibility (it owns the tables); this module only parses and cleanses.
 */
@Service
public class EmployeeImportService {

	@Value("${import.employee-batch-size}")
	private int batchSize;

	private static final Logger log = LoggerFactory.getLogger(EmployeeImportService.class);

	private final DirectoryIngestionService ingestionService;

	public EmployeeImportService(DirectoryIngestionService ingestionService) {
		this.ingestionService = ingestionService;
	}

	public void importEmployee(Path empCsv) {

		final Instant importStartedAt = ingestionService.beginEmployeeImport();

		final CsvMapper csvMapper = new CsvMapper();
		final CsvSchema schema = buildEmployeeSchema();

		final List<EmployeeRecord> batch = new ArrayList<>(batchSize);
		int processed = 0;

		try (BufferedReader reader = Files.newBufferedReader(empCsv, Charset.forName("Windows-1252"))) {
			final MappingIterator<EmployeeDTO> it = csvMapper.readerFor(EmployeeDTO.class)
				.with(schema)
				.readValues(reader);
			while (it.hasNext()) {

				final var row = it.next();

				batch.add(new EmployeeRecord(
					cleanGuid(row.PersonId),
					ImportUtil.nullIfNullString(row.Givenname),
					ImportUtil.nullIfNullString(row.Lastname),
					ImportUtil.nullIfNullString(row.WorkMobile),
					ImportUtil.nullIfNullString(row.WorkPhone),
					ImportUtil.nullIfNullString(row.Title),
					ImportUtil.nullIfNullString(row.OrgId),
					ImportUtil.nullIfNullString(row.PrimaryEMailAddress),
					cleanGuid(row.ManagerId),
					ImportUtil.nullIfNullString(row.ManagerCode)));

				if (batch.size() >= batchSize) {
					ingestionService.upsertEmployees(batch);
					processed += batch.size();
					batch.clear();
				}
			}
			if (!batch.isEmpty()) {
				ingestionService.upsertEmployees(batch);
				processed += batch.size();
			}
			log.info("[EMP] final upsert complete. Rows sent to DB: {}", processed);

		} catch (IOException e) {
			throw new RuntimeException("Error Importing organization from:" + empCsv.getFileName().toAbsolutePath(), e);
		}

		final int deactivated = ingestionService.deactivateEmployeesNotSeenSince(importStartedAt);
		log.info("[EMP] non updated employees set to inactive: {}", deactivated);
	}

	private CsvSchema buildEmployeeSchema() {
		return CsvSchema.emptySchema()
			.withHeader()
			.withColumnSeparator(';');
	}

	private String cleanGuid(String guid) {
		guid = ImportUtil.nullIfNullString(guid);
		if (guid == null) {
			return null;
		}
		return guid.replaceAll("[{}]", "").trim();
	}
}
