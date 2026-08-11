package me.ronygomes.ums.api.validator;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.dto.EnrollmentDto;
import me.ronygomes.ums.api.model.CourseSchedule;
import me.ronygomes.ums.api.model.EnrollmentStatus;
import me.ronygomes.ums.api.repository.CourseScheduleRepository;
import me.ronygomes.ums.api.repository.EnrollmentRepository;
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
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentValidator(Validator validator,
                               CourseScheduleRepository courseScheduleRepository,
                               StudentRepository studentRepository,
                               EnrollmentRepository enrollmentRepository) {

        this.validator = validator;
        this.courseScheduleRepository = courseScheduleRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
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

        Optional<CourseSchedule> scheduleOpt = Optional.empty();
        if (!errors.hasFieldErrors("courseScheduleId")) {
            scheduleOpt = courseScheduleRepository.findById(dto.getCourseScheduleId());
            if (scheduleOpt.isEmpty()) {
                errors.rejectValue("courseScheduleId", null, "course schedule not found");
            } else if (!scheduleOpt.get().isEnrollmentOpen()) {
                errors.rejectValue("courseScheduleId", null, "enrollment is closed for this course schedule");
            }
        }

        if (!errors.hasFieldErrors("studentId") && studentRepository.findById(dto.getStudentId()).isEmpty()) {
            errors.rejectValue("studentId", null, "student not found");
        }

        if (dto.getStatus() == EnrollmentStatus.ON_GOING
                && scheduleOpt.isPresent()
                && !errors.hasFieldErrors("courseScheduleId")
                && !errors.hasFieldErrors("studentId")) {

            Long courseId = scheduleOpt.get().getCourseId();
            boolean hasOngoingForCourse = enrollmentRepository.findByStudentId(dto.getStudentId()).stream()
                    .filter(e -> !e.getCourseSchedule().getId().equals(dto.getCourseScheduleId()))
                    .filter(e -> e.getStatus() == EnrollmentStatus.ON_GOING)
                    .anyMatch(e -> e.getCourseSchedule().getCourseId().equals(courseId));

            if (hasOngoingForCourse) {
                errors.rejectValue("courseScheduleId", null,
                        "student already has an ongoing enrollment for this course");
            }
        }
    }
}
