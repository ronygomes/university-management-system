package me.ronygomes.ums.api.validator;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.model.Student;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.util.Objects;

@Component
public class StudentValidator implements org.springframework.validation.Validator {

    private final DepartmentRepository departmentRepository;
    private final Validator validator;

    public StudentValidator(DepartmentRepository departmentRepository,
                            Validator validator) {

        this.departmentRepository = departmentRepository;
        this.validator = validator;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Student.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Student student = (Student) target;

        SpringValidatorAdapter beanValidator = new SpringValidatorAdapter(validator);
        beanValidator.validate(student, errors);

        if (Objects.isNull(student.getDepartmentCode())) {
            errors.rejectValue("departmentCode", null, "must not be null");
        } else if (departmentRepository.findByCode(student.getDepartmentCode()).isEmpty()) {
            errors.rejectValue("departmentCode", null, "department not found");
        }
    }
}
