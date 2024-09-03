package me.ronygomes.ums.api.validator;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.dto.EnrollmentDto;
import me.ronygomes.ums.api.repository.CourseRepository;
import me.ronygomes.ums.api.repository.StudentRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

@Component
public class EnrollmentValidator implements org.springframework.validation.Validator {

    private final Validator validator;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public EnrollmentValidator(Validator validator,
                               CourseRepository courseRepository,
                               StudentRepository studentRepository) {

        this.validator = validator;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return EnrollmentDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EnrollmentDto dto = (EnrollmentDto) target;

        SpringValidatorAdapter beanValidator = new SpringValidatorAdapter(validator);
        beanValidator.validate(dto, errors);

        if (!errors.hasFieldErrors("courseId") && courseRepository.findById(dto.getCourseId()).isEmpty()) {
            errors.rejectValue("courseId", null, "course not found");
        }

        if (!errors.hasFieldErrors("studentId") && studentRepository.findById(dto.getStudentId()).isEmpty()) {
            errors.rejectValue("studentId", null, "student not found");
        }
    }
}
