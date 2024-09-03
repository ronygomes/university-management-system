package me.ronygomes.ums.api.service;

import jakarta.transaction.Transactional;
import me.ronygomes.ums.api.dto.EnrollmentDto;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Enrollment;
import me.ronygomes.ums.api.repository.CourseRepository;
import me.ronygomes.ums.api.repository.EnrollmentRepository;
import me.ronygomes.ums.api.repository.StudentRepository;
import me.ronygomes.ums.api.validator.EnrollmentValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static me.ronygomes.ums.api.exception.ExceptionType.ENTITY_NOT_FOUND;

@Service
public class EnrollmentService {

    private static final String FIND_BY_ID_ERROR_DETAILS_TEMPLATE = "Enrollment with id=%s not found";
    private static final String DATA_VALIDATION_ERROR_DETAILS_TEMPLATE = "Not a valid Enrollment. See 'error' field for details";

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentValidator enrollmentValidator;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final ExceptionHelper exceptionHelper;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             EnrollmentValidator enrollmentValidator,
                             CourseRepository courseRepository,
                             StudentRepository studentRepository,
                             ExceptionHelper exceptionHelper) {

        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentValidator = enrollmentValidator;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.exceptionHelper = exceptionHelper;
    }

    public EnrollmentDto findById(long id) {
        return new EnrollmentDto(findByIdOrThrow(id));
    }

    @Transactional
    public long create(EnrollmentDto enrollmentDto) {
        validateOrThrow(enrollmentDto);
        return save(null, enrollmentDto);
    }

    @Transactional
    public void update(Long id, EnrollmentDto enrollmentDto) {
        findByIdOrThrow(id);
        validateOrThrow(enrollmentDto);
        save(id, enrollmentDto);
    }

    @Transactional
    public void updateProvided(Long id, EnrollmentDto enrollmentDto) {
        Enrollment dbEnrollment = findByIdOrThrow(id);
        enrollmentDto.mergeWith(dbEnrollment);
        validateOrThrow(enrollmentDto);
        save(id, enrollmentDto);
    }

    @Transactional
    public void delete(Long id) {
        Enrollment e = findByIdOrThrow(id);
        enrollmentRepository.delete(e);
    }

    private Enrollment findByIdOrThrow(long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new UmsDataException(ENTITY_NOT_FOUND,
                        FIND_BY_ID_ERROR_DETAILS_TEMPLATE.formatted(id)));
    }

    private void validateOrThrow(EnrollmentDto dto) {
        BindingResult errors = new BeanPropertyBindingResult(dto, "enrollmentDto");
        enrollmentValidator.validate(dto, errors);
        exceptionHelper.throwErrorIfValidationError(errors, DATA_VALIDATION_ERROR_DETAILS_TEMPLATE);
    }

    private long save(Long id, EnrollmentDto enrollmentDto) {
        Enrollment e = enrollmentDto.toEnrollment(id);
        e.setCourse(courseRepository.findById(enrollmentDto.getCourseId()).orElseThrow());
        e.setStudent(studentRepository.findById(enrollmentDto.getStudentId()).orElseThrow());
        enrollmentRepository.save(e);

        return e.getId();
    }
}
