package me.ronygomes.ums.api.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import me.ronygomes.ums.api.dto.CourseDto;
import me.ronygomes.ums.api.dto.CoursePatchDto;
import me.ronygomes.ums.api.exception.ErrorMessage;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Semester;
import me.ronygomes.ums.api.model.Teacher;
import me.ronygomes.ums.api.repository.CourseRepository;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import me.ronygomes.ums.api.repository.TeacherRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static me.ronygomes.ums.api.exception.ExceptionType.DATA_VALIDATION_FAILED;
import static me.ronygomes.ums.api.exception.ExceptionType.ENTITY_NOT_FOUND;

@Service
public class CourseService {

    private static final String FIND_BY_CODE_ERROR_DETAILS_TEMPLATE = "Course with id '%d' not found";
    private static final String DATA_VALIDATION_ERROR_DETAILS_TEMPLATE = "Not a valid Course. See 'error' field for details";

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;
    private final ExceptionHelper exceptionHelper;
    private final Validator validator;

    public CourseService(CourseRepository courseRepository,
                         DepartmentRepository departmentRepository,
                         TeacherRepository teacherRepository,
                         ExceptionHelper exceptionHelper,
                         Validator validator) {

        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.teacherRepository = teacherRepository;
        this.exceptionHelper = exceptionHelper;
        this.validator = validator;
    }

    public List<CourseDto> findAll() {
        return courseRepository.findAll().stream().map(CourseDto::toDto).toList();
    }

    public Page<CourseDto> findPaged(String departmentCode, Semester semester, Pageable pageable) {
        return courseRepository.findFiltered(departmentCode, semester, pageable).map(CourseDto::toDto);
    }

    public CourseDto findById(Long id) {
        return CourseDto.toDto(findByIdOrThrow(id));
    }

    @Transactional
    public long create(CourseDto dto) {
        Course course = new Course();
        validateThenCopy(dto, course);

        save(course);
        return course.getId();
    }

    @Transactional
    public void updateAll(long id, CourseDto dto) {
        Course dbCourse = findByIdOrThrow(id);
        validateThenCopy(dto, dbCourse);

        save(dbCourse);
    }

    @Transactional
    public void updateProvided(long id, CoursePatchDto patchDto) {
        Course dbCourse = findByIdOrThrow(id);
        CourseDto inputDto = patchDto.toInputDto(dbCourse);
        validateThenCopy(inputDto, dbCourse);

        save(dbCourse);
    }

    public void delete(Long id) {
        courseRepository.delete(findByIdOrThrow(id));
    }

    private Course findByIdOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new UmsDataException(ENTITY_NOT_FOUND,
                        FIND_BY_CODE_ERROR_DETAILS_TEMPLATE.formatted(id)));
    }

    private void save(Course course) {
        try {
            courseRepository.save(course);
            courseRepository.flush();
        } catch (TransactionSystemException | DataIntegrityViolationException tse) {
            throw new UmsDataException(DATA_VALIDATION_FAILED,
                    DATA_VALIDATION_ERROR_DETAILS_TEMPLATE, exceptionHelper.extractConstraintViolation(tse));
        }
    }

    private void validateThenCopy(CourseDto from, Course to) {
        var dataErrors = validator.validate(from);
        List<ErrorMessage> errors = new ArrayList<>(dataErrors
                .stream()
                .map(e -> new ErrorMessage(e.getPropertyPath().toString(), e.getMessage())).toList());

        Optional<Department> department = departmentRepository.findByCode(from.getDepartmentCode());
        if (department.isEmpty()) {
            errors.add(new ErrorMessage("departmentCode", "Department code not found"));
        }

        List<Teacher> instructors = new ArrayList<>();
        if (Objects.nonNull(from.getInstructorIds())) {
            for (Long instructorId : from.getInstructorIds()) {
                Optional<Teacher> instructor = teacherRepository.findById(instructorId);
                if (instructor.isEmpty()) {
                    errors.add(new ErrorMessage("instructorIds", "Teacher not found: " + instructorId));
                } else {
                    instructors.add(instructor.get());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new UmsDataException(ExceptionType.DATA_VALIDATION_FAILED,
                    DATA_VALIDATION_ERROR_DETAILS_TEMPLATE, errors);
        }

        from.copy(to, department.orElseThrow(), instructors);
    }
}
