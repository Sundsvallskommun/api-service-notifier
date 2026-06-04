package se.sundsvall.notifier.users.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
import se.sundsvall.notifier.users.api.validation.impl.MunicipalityNameValidator;

@Documented
@Constraint(validatedBy = MunicipalityNameValidator.class)
@Target({
	ElementType.METHOD, ElementType.FIELD,
	ElementType.ANNOTATION_TYPE,
	ElementType.CONSTRUCTOR,
	ElementType.PARAMETER,
	ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMunicipalityName {
	String message() default "must be a valid municipality name";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
