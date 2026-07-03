package se.sundsvall.notifier.messaging.service.utility;

import org.springframework.stereotype.Component;

@Component
public class PhoneNumberUtil {
	public String cleanPhoneNumber(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.isBlank()) {
			return null;
		}
		phoneNumber = phoneNumber.replaceAll("[^0-9]", "");

		if (phoneNumber.startsWith("0")) {
			phoneNumber = "+46" + phoneNumber.substring(1);
		} else if (phoneNumber.startsWith("46")) {
			phoneNumber = "+" + phoneNumber;
		}

		if (phoneNumber.matches("^\\+[1-9][\\d]{3,14}$")) {
			return phoneNumber;
		}
		return null;
	}
}
