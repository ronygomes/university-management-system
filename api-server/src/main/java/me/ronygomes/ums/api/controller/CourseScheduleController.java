package me.ronygomes.ums.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import me.ronygomes.ums.api.config.annotation.AdminAccess;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.CourseSchedule;
import me.ronygomes.ums.api.repository.CourseRepository;
import me.ronygomes.ums.api.repository.CourseScheduleRepository;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import me.ronygomes.ums.api.validator.CourseScheduleValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static me.ronygomes.ums.api.exception.ExceptionType.ENTITY_NOT_FOUND;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1/schedules")
public class CourseScheduleController {

    private static final String FIND_BY_CODE_ERROR_DETAILS_TEMPLATE = "Course Schedule with id '%d' not found";
    private static final String DATA_VALIDATION_ERROR_DETAILS_TEMPLATE = "Not a valid Course Schedule. See 'error' field for details";

    private final CourseScheduleRepository courseScheduleRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final Validator validator;
    private final CourseScheduleValidator courseScheduleValidator;
    private final ExceptionHelper exceptionHelper;

    public CourseScheduleController(CourseScheduleRepository courseScheduleRepository,
                                    DepartmentRepository departmentRepository,
                                    CourseRepository courseRepository,
                                    Validator validator,
                                    CourseScheduleValidator courseScheduleValidator,
                                    ExceptionHelper exceptionHelper) {

        this.courseScheduleRepository = courseScheduleRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.validator = validator;
        this.courseScheduleValidator = courseScheduleValidator;
        this.exceptionHelper = exceptionHelper;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(courseScheduleValidator);
    }

    @AdminAccess
    @GetMapping
    public List<CourseSchedule> findAll() {
        return courseScheduleRepository.findAll();
    }

    @AdminAccess
    @GetMapping("/{id}")
    public CourseSchedule findById(@PathVariable Long id) {
        return findByIdOrThrow(id);
    }

    @AdminAccess
    @GetMapping("/course/{id}")
    public List<CourseSchedule> findAllByCourseId(@PathVariable Long id) {
        return courseScheduleRepository.findByCourseId(id);
    }

    @AdminAccess
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CourseSchedule cs,
                                    BindingResult errors) {

        exceptionHelper.throwErrorIfValidationError(errors, DATA_VALIDATION_ERROR_DETAILS_TEMPLATE, "course", "department");
        save(cs);

        return ResponseEntity.created(linkTo(CourseScheduleController.class).slash(cs.getId()).toUri())
                .build();
    }

    @AdminAccess
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody CourseSchedule cs,
                                    BindingResult errors) {

        findByIdOrThrow(id);
        exceptionHelper.throwErrorIfValidationError(errors, DATA_VALIDATION_ERROR_DETAILS_TEMPLATE, "course", "department");
        cs.setId(id);
        save(cs);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(CourseScheduleController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePatch(@PathVariable Long id,
                                         @RequestBody CourseSchedule cs) {

        CourseSchedule dbCs = findByIdOrThrow(id);
        BindingResult errors = validateAndMerge(dbCs, cs);

        exceptionHelper.throwErrorIfValidationError(errors, DATA_VALIDATION_ERROR_DETAILS_TEMPLATE, "course", "department");
        save(dbCs);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(CourseScheduleController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        CourseSchedule schedule = findByIdOrThrow(id);
        courseScheduleRepository.delete(schedule);

        return ResponseEntity.accepted().build();
    }

    private CourseSchedule findByIdOrThrow(Long id) {
        return courseScheduleRepository.findById(id)
                .orElseThrow(() -> new UmsDataException(ENTITY_NOT_FOUND,
                        FIND_BY_CODE_ERROR_DETAILS_TEMPLATE.formatted(id)));
    }

    private void save(CourseSchedule cs) {
        cs.setDepartment(departmentRepository.findByCode(cs.getDepartmentCode()).orElseThrow());
        cs.setCourse(courseRepository.findById(cs.getCourseId()).orElseThrow());
        courseScheduleRepository.save(cs);
    }

    private BindingResult validateAndMerge(CourseSchedule dbCs, CourseSchedule cs) {
        dbCs.setCourseId(dbCs.getCourse().getId());
        dbCs.setDepartmentCode(dbCs.getDepartment().getCode());
        dbCs.merge(cs);

        SpringValidatorAdapter beanValidator = new SpringValidatorAdapter(validator);
        BindingResult errors = new BeanPropertyBindingResult(dbCs, "courseSchedule");

        beanValidator.validate(dbCs, errors);
        courseScheduleValidator.validate(dbCs, errors);

        return errors;
    }
}
