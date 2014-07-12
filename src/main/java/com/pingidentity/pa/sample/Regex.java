/**************************************************************************
 * Copyright (C) 2014 Ping Identity Corporation
 * All rights reserved.
 *
 * The contents of this file are subject to the Apache License,
 * Version 2.0, available at: http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS SOFTWARE IS PROVIDED ?AS IS?, WITHOUT ANY WARRANTIES, EXPRESS,
 * IMPLIED, STATUTORY OR ARISING BY CUSTOM OR TRADE USAGE, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * TITLE, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. THE USER
 * ASSUMES ALL RISK ASSOCIATED WITH ACCESS  AND USE OF THE SOFTWARE.
 **************************************************************************/

package com.pingidentity.pa.sample;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

@Documented
@Constraint(validatedBy = { Regex.RegexValidator.class })
@Target({ ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface Regex {

    String message() default "Must be a valid Java Regex";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class RegexValidator implements ConstraintValidator<Regex, String> {

        public void initialize(Regex constraintAnnotation) {
        }

        public boolean isValid(String toValidate, ConstraintValidatorContext constraintContext) {

            boolean isValid = true;
            try {
                if (toValidate !=  null) {
                    Pattern.compile(toValidate);
                } else {
                    isValid = false;
                }
            } catch (PatternSyntaxException e) {
                isValid = false;
            }
            return isValid;

        }
    }


}
