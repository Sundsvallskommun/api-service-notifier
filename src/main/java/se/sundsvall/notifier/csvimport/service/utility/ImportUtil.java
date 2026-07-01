package se.sundsvall.notifier.csvimport.service.utility;

import java.util.Optional;

public final class ImportUtil {

	private static final String NULL_LITERAL = "NULL";

	private ImportUtil() {}

	/**
	 * Trims the CSV value and normalises "empty" values to {@code null}: blank strings and the
	 * literal text {@code "NULL"} (case-insensitive) both become {@code null}.
	 */
	public static String sanitize(final String value) {
		return Optional.ofNullable(value)
			.map(String::trim)
			.filter(trimmed -> !trimmed.isEmpty())
			.filter(trimmed -> !trimmed.equalsIgnoreCase(NULL_LITERAL))
			.orElse(null);
	}
}
