package se.sundsvall.notifier.users.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.dept44.util.MunicipalityUtils;
import se.sundsvall.notifier.users.api.validation.ValidMunicipalityName;

public class MunicipalityNameValidator implements ConstraintValidator<ValidMunicipalityName, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return false;
		}
		return MunicipalityUtils.existsByName(value) || MunicipalityUtils.existsById(value);
	}
}
