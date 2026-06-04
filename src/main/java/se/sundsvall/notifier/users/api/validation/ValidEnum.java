package se.sundsvall.notifier.users.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
import se.sundsvall.notifier.users.api.validation.impl.EnumValidator;

@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({
	ElementType.METHOD, ElementType.FIELD,
	ElementType.ANNOTATION_TYPE,
	ElementType.CONSTRUCTOR,
	ElementType.PARAMETER,
	ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {
	String message() default "Invalid value for enum";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	Class<? extends Enum<?>> enumClass();

	boolean ignoreCase() default false;
}
