package se.sundsvall.notifier.users.api.validation.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MunicipalityNameValidatorTest {

	private final MunicipalityNameValidator validator = new MunicipalityNameValidator();

	@Test
	void nullValueIsInvalid() {
		assertThat(validator.isValid(null, null)).isFalse();
	}

	@Test
	void validMunicipalityNameIsValid() {
		assertThat(validator.isValid("Sundsvall", null)).isTrue();
	}

	@Test
	void validMunicipalityIdIsValid() {
		assertThat(validator.isValid("2281", null)).isTrue();
	}

	@Test
	void unknownMunicipalityIsInvalid() {
		assertThat(validator.isValid("NotARealMunicipality", null)).isFalse();
	}
}
