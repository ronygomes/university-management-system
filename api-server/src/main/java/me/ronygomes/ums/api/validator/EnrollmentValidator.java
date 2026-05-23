package me.ronygomes.ums.api.validator;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.dto.EnrollmentDto;
import me.ronygomes.ums.api.model.CourseSchedule;
import me.ronygomes.ums.api.repository.CourseScheduleRepository;
import me.ronygomes.ums.api.repository.StudentRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.util.Optional;

@Component
public class EnrollmentValidator implements org.springframework.validation.Validator {

    private final Validator validator;
    private final CourseScheduleRepository courseScheduleRepository;
    private final StudentRepository studentRepository;

    public EnrollmentValidator(Validator validator,
                               CourseScheduleRepository courseScheduleRepository,
                               StudentRepository studentRepository) {

        this.validator = validator;
        this.courseScheduleRepository = courseScheduleRepository;
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

        if (!errors.hasFieldErrors("courseScheduleId")) {
            Optional<CourseSchedule> opt = courseScheduleRepository.findById(dto.getCourseScheduleId());
            if (opt.isEmpty()) {
                errors.rejectValue("courseScheduleId", null, "course schedule not found");
            } else if (!opt.get().isEnrollmentOpen()) {
                errors.rejectValue("courseScheduleId", null, "enrollment is closed for this course schedule");
            }
        }

        if (!errors.hasFieldErrors("studentId") && studentRepository.findById(dto.getStudentId()).isEmpty()) {
            errors.rejectValue("studentId", null, "student not found");
        }
    }
}
