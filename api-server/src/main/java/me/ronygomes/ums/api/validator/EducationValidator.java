package me.ronygomes.ums.api.validator;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.model.Education;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

@Component
public class EducationValidator implements org.springframework.validation.Validator {

    private final Validator validator;

    public EducationValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Education.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Education education = (Education) target;

        SpringValidatorAdapter beanValidator = new SpringValidatorAdapter(validator);
        beanValidator.validate(education, errors);
    }
}
