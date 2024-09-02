package me.ronygomes.ums.api.validator;

import me.ronygomes.ums.api.model.CourseSchedule;
import me.ronygomes.ums.api.repository.CourseRepository;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

@Component
public class CourseScheduleValidator implements Validator {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;

    public CourseScheduleValidator(DepartmentRepository departmentRepository,
                                   CourseRepository courseRepository) {

        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return CourseSchedule.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CourseSchedule cs = (CourseSchedule) target;

        if (Objects.isNull(cs.getDepartmentCode())) {
            errors.rejectValue("departmentCode", null, "must not be null");
        } else if (departmentRepository.findByCode(cs.getDepartmentCode()).isEmpty()) {
            errors.rejectValue("departmentCode", null, "department code not found");
        }

        if (Objects.isNull(cs.getCourseId())) {
            errors.rejectValue("courseId", null, "must not be null");
        } else if (courseRepository.findById(cs.getCourseId()).isEmpty()) {
            errors.rejectValue("courseId", null, "course does not exists");
        }
    }
}
