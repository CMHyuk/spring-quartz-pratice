package com.example.quartz.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private String fieldName;
    private Object[] enumConstants;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.fieldName = constraintAnnotation.fieldName();
        this.enumConstants = constraintAnnotation.enumClass().getEnumConstants();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext context) {

        if (StringUtils.isEmpty(s)) {
            return buildConstraintViolationWithTemplate(context, "{0} is required");
        }

        for (Object enumConstant : this.enumConstants) {
            if (StringUtils.equals(enumConstant.toString(), s)) {
                return true;
            }
        }

        return buildConstraintViolationWithTemplate(context, "{0} must be one of the following values " +
                String.join(",", Arrays.toString(enumConstants)));

    }

    private boolean buildConstraintViolationWithTemplate(ConstraintValidatorContext context, String format) {
        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(MessageFormat.format(format, fieldName)).addConstraintViolation();
        return false;
    }
}
