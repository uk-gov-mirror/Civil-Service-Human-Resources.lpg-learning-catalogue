package uk.gov.cslearning.catalogue.api.validators.url;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class SlugValidator implements ConstraintValidator<Slug, String> {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    @Override
    public void initialize(Slug constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return SLUG_PATTERN.matcher(value).matches();
    }
}
