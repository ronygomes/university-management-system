package me.ronygomes.ums.api.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import me.ronygomes.ums.api.dto.TeacherDto;
import me.ronygomes.ums.api.dto.TeacherPatchInputDto;
import me.ronygomes.ums.api.exception.ErrorMessage;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Designation;
import me.ronygomes.ums.api.model.Teacher;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import me.ronygomes.ums.api.repository.DesignationRepository;
import me.ronygomes.ums.api.repository.TeacherRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.ronygomes.ums.api.exception.ExceptionType.DATA_VALIDATION_FAILED;
import static me.ronygomes.ums.api.exception.ExceptionType.ENTITY_NOT_FOUND;

@Service
public class TeacherService {

    private static final String FIND_BY_ID_ERROR_DETAILS_TEMPLATE = "Teacher with id=%s not found";
    private static final String DATA_VALIDATION_ERROR_DETAILS_TEMPLATE = "Not a valid Teacher. See 'error' field for details";

    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final Validator validator;
    private final ExceptionHelper exceptionHelper;

    public TeacherService(TeacherRepository teacherRepository,
                          DepartmentRepository departmentRepository,
                          DesignationRepository designationRepository,
                          Validator validator,
                          ExceptionHelper exceptionHelper) {

        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.validator = validator;
        this.exceptionHelper = exceptionHelper;
    }

    public List<Teacher> findAll() {
        return teacherRepository.findAll();
    }

    public Teacher findById(long id) {
        return findByIdOrThrow(id);
    }

    @Transactional
    public long create(TeacherDto teacherDto) {
        Teacher teacher = new Teacher();
        validateThenCopy(teacherDto, teacher);

        save(teacher);
        return teacher.getId();
    }

    @Transactional
    public void updateAll(long id, TeacherDto teacherDto) {
        Teacher dbTeacher = findById(id);
        validateThenCopy(teacherDto, dbTeacher);

        save(dbTeacher);
    }

    @Transactional
    public void updateProvided(long id, TeacherPatchInputDto patchDto) {
        Teacher dbTeacher = findById(id);
        TeacherDto inputDto = patchDto.toInputDto(dbTeacher);
        validateThenCopy(inputDto, dbTeacher);

        save(dbTeacher);
    }

    @Transactional
    public void delete(long id) {
        teacherRepository.delete(findById(id));
    }

    private void validateThenCopy(TeacherDto teacher, Teacher destination) {
        var dataErrors = validator.validate(teacher);
        List<ErrorMessage> errors = new ArrayList<>(dataErrors
                .stream()
                .map(e -> new ErrorMessage(e.getPropertyPath().toString(), e.getMessage())).toList());

        Optional<Department> department = departmentRepository.findByCode(teacher.getDepartmentCode());
        if (department.isEmpty()) {
            errors.add(new ErrorMessage("departmentCode", "Department code not found"));
        }

        Optional<Designation> designation = designationRepository.findByTitle(teacher.getTitle());
        if (designation.isEmpty()) {
            errors.add(new ErrorMessage("title", "Title not found"));
        }

        if (!errors.isEmpty()) {
            throw new UmsDataException(ExceptionType.DATA_VALIDATION_FAILED,
                    DATA_VALIDATION_ERROR_DETAILS_TEMPLATE, errors);
        }

        destination.setFullName(teacher.getFullName());
        destination.setAddress(teacher.getAddress());
        destination.setEmail(teacher.getEmail());
        destination.setContactNumber(teacher.getContactNumber());
        destination.setAssignedCredit(teacher.getAssignedCredit());
        destination.setDepartment(department.orElseThrow());
        destination.setDesignation(designation.orElseThrow());
    }

    private Teacher findByIdOrThrow(long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new UmsDataException(ENTITY_NOT_FOUND,
                        FIND_BY_ID_ERROR_DETAILS_TEMPLATE.formatted(id)));
    }

    private void save(Teacher teacher) {
        try {
            teacherRepository.save(teacher);
            teacherRepository.flush();
        } catch (TransactionSystemException | DataIntegrityViolationException tse) {
            throw new UmsDataException(DATA_VALIDATION_FAILED,
                    DATA_VALIDATION_ERROR_DETAILS_TEMPLATE, exceptionHelper.extractConstraintViolation(tse));
        }
    }
}
