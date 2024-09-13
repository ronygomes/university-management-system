package me.ronygomes.ums.api.validator.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.OverridesAttribute;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Pattern(regexp = ContactNumber.REGEX_PATTERN)
@Constraint(validatedBy = {})
public @interface ContactNumber {

    String REGEX_PATTERN = "^\\+\\d{13}$";

    @OverridesAttribute(constraint = Pattern.class, name = "message")
    String message() default "invalid contact number format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
