package se.sundsvall.notifier.csvimport.service.utility;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ImportUtilTest {

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {
		"", "   ", "\t", "NULL", "null", "  NuLl  "
	})
	void returnsNullForEmptyOrNullLiteral(final String input) {
		assertThat(ImportUtil.sanitize(input)).isNull();
	}

	@ParameterizedTest
	@CsvSource({
		"value, value",
		"'  padded  ', padded",
		"'NULLIFIED', NULLIFIED"
	})
	void trimsAndKeepsRealValues(final String input, final String expected) {
		assertThat(ImportUtil.sanitize(input)).isEqualTo(expected);
	}
}
