package se.sundsvall.notifier.messaging.service.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberUtilTest {

	private PhoneNumberUtil phoneNumberUtil;

	@BeforeEach
	void setUp() {
		phoneNumberUtil = new PhoneNumberUtil();
	}

	@Test
	void cleanPhoneNumberSpecialCharactersTest() {
		var result = phoneNumberUtil.cleanPhoneNumber("070-123 45 67");

		assertThat(result).isEqualTo("+46701234567");
	}

	@Test
	void cleanPhoneNumberAddPlusTest() {
		var result = phoneNumberUtil.cleanPhoneNumber("46701234567");

		assertThat(result).isEqualTo("+46701234567");
	}

	@Test
	void cleanPhoneNumberAlreadyValidTest() {
		var result = phoneNumberUtil.cleanPhoneNumber("+46701234567");

		assertThat(result).isEqualTo("+46701234567");
	}

	@Test
	void cleanPhoneNumberInvalidNumberTest() {
		var result = phoneNumberUtil.cleanPhoneNumber("123");

		assertThat(result).isNull();
	}

	@Test
	void cleanPhoneNumberNullTest() {
		assertThat(phoneNumberUtil.cleanPhoneNumber(null)).isNull();
	}

	@Test
	void cleanPhoneNumberBlankTest() {
		assertThat(phoneNumberUtil.cleanPhoneNumber("   ")).isNull();
	}
}
