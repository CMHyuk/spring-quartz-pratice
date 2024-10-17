package com.example.quartz.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Map;

public class MapValidator implements
        ConstraintValidator<ValidMap, Map<String, ?>> {

    private String fieldName;

    @Override
    public void initialize(ValidMap constraintAnnotation) {
        this.fieldName = constraintAnnotation.fieldName();
    }

    @Override
    public boolean isValid(final Map<String, ?> map,
                           final ConstraintValidatorContext context) {

        if (map == null) {
            return buildConstraintViolationWithTemplate(context, "{0} is required");
        }

        if (map.isEmpty()) {
            return buildConstraintViolationWithTemplate(context, "{0} is need key & value");
        }

        if (map.keySet().stream().anyMatch(StringUtils::isEmpty)) {
            return buildConstraintViolationWithTemplate(context, "key of {0} must be not empty");
        }

        return true;
    }

    private boolean buildConstraintViolationWithTemplate(ConstraintValidatorContext context, String format) {
        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(MessageFormat.format(format, fieldName)).addConstraintViolation();
        return false;
    }
}
