package se.sundsvall.notifier.csvimport.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.sundsvall.notifier.csvimport.db.dto.OrganizationDTO;
import se.sundsvall.notifier.csvimport.service.utility.ImportUtil;
import se.sundsvall.notifier.messaging.ingestion.DirectoryIngestionService;
import se.sundsvall.notifier.messaging.ingestion.OrganizationRecord;
import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

/**
 * Parses the organization CSV and hands batches to the messaging module's
 * {@link DirectoryIngestionService}. This module no longer writes the {@code organization} table
 * directly — it only parses and cleanses.
 */
@Service
public class OrganizationImportService {

	@Value("${import.organization-batch-size}")
	private int batchSize;

	private static final Logger log = LoggerFactory.getLogger(OrganizationImportService.class);

	private final DirectoryIngestionService ingestionService;

	public OrganizationImportService(DirectoryIngestionService ingestionService) {
		this.ingestionService = ingestionService;
	}

	public void importOrganizations(Path orgCsv) {

		final CsvMapper csvMapper = new CsvMapper();
		final CsvSchema schema = buildOrganizationSchema();

		final List<OrganizationRecord> batch = new ArrayList<>(batchSize);
		int processed = 0;

		try (BufferedReader reader = Files.newBufferedReader(orgCsv, Charset.forName("Windows-1252"))) {

			final MappingIterator<OrganizationDTO> it = csvMapper.readerFor(OrganizationDTO.class)
				.with(schema)
				.readValues(reader);
			while (it.hasNext()) {

				final var row = it.next();

				batch.add(new OrganizationRecord(
					ImportUtil.nullIfNullString(row.CompanyId),
					ImportUtil.nullIfNullString(row.OrgId),
					ImportUtil.nullIfNullString(row.OrgName),
					ImportUtil.nullIfNullString(row.ParentId),
					ImportUtil.nullIfNullString(row.TreeLevel)));

				if (batch.size() >= batchSize) {
					ingestionService.upsertOrganizations(batch);
					processed += batch.size();
					batch.clear();
				}
			}
			if (!batch.isEmpty()) {
				ingestionService.upsertOrganizations(batch);
				processed += batch.size();
			}
			ingestionService.ensureUnknownOrganization();
			log.info("[ORG] final upsert complete. Rows sent to DB: {}", processed);

		} catch (IOException e) {
			throw new RuntimeException("Error Importing organization from:" + orgCsv.getFileName().toAbsolutePath(), e);
		}
	}

	private CsvSchema buildOrganizationSchema() {
		return CsvSchema.emptySchema()
			.withHeader()
			.withColumnSeparator(';');
	}
}
