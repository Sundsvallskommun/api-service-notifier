package se.sundsvall.notifier.users.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.notifier.users.api.validation.ValidEnum;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

	private Class<? extends Enum<?>> enumClass;
	private boolean ignoreCase;

	@Override
	public void initialize(ValidEnum annotation) {
		this.enumClass = annotation.enumClass();
		this.ignoreCase = annotation.ignoreCase();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		for (Enum<?> enumVal : enumClass.getEnumConstants()) {
			if (ignoreCase) {
				if (enumVal.name().equalsIgnoreCase(value)) {
					return true;
				}
			} else {
				if (enumVal.name().equals(value)) {
					return true;
				}
			}
		}
		return false;
	}
}
