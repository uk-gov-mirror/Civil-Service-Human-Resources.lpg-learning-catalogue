package uk.gov.cslearning.catalogue.api.validators.url;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SlugValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Slug {

    // Default error message
    String message() default "Invalid slug. Must contain only lowercase letters, numbers, and hyphens, and cannot start or end with a hyphen.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
