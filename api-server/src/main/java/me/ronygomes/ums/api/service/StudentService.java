package me.ronygomes.ums.api.service;

import jakarta.transaction.Transactional;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Education;
import me.ronygomes.ums.api.model.Student;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import me.ronygomes.ums.api.repository.RegistrationNumberRepository;
import me.ronygomes.ums.api.repository.StudentRepository;
import me.ronygomes.ums.api.validator.EducationValidator;
import me.ronygomes.ums.api.validator.StudentValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.Date;

import static me.ronygomes.ums.api.exception.ExceptionType.ENTITY_NOT_FOUND;

@Service
public class StudentService {

    private static final String FIND_BY_CODE_ERROR_DETAILS_TEMPLATE = "Student with id '%d' not found";
    private static final String DATA_VALIDATION_ERROR_DETAILS_TEMPLATE = "Not a valid Student. See 'error' field for details";
    private static final String FIND_BY_ID_EDUCATION_ERROR_DETAILS_TEMPLATE = "Education with id '%d' not found";
    private static final String DATA_VALIDATION_ERROR_EDUCATION_DETAILS_TEMPLATE = "Not a valid Education. See 'error' field for details";

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final RegistrationNumberRepository registrationNumberRepository;
    private final StudentValidator studentValidator;
    private final EducationValidator educationValidator;
    private final ExceptionHelper exceptionHelper;

    public StudentService(StudentRepository studentRepository,
                          DepartmentRepository departmentRepository,
                          RegistrationNumberRepository registrationNumberRepository,
                          StudentValidator studentValidator,
                          EducationValidator educationValidator,
                          ExceptionHelper exceptionHelper) {

        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.registrationNumberRepository = registrationNumberRepository;
        this.studentValidator = studentValidator;
        this.educationValidator = educationValidator;
        this.exceptionHelper = exceptionHelper;
    }

    public java.util.List<Student> findAll() {
        return studentRepository.findAll();
    }

    public java.util.Optional<Student> findByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    public Student findById(long id) {
        return findWithEducationOrThrow(id);
    }

    @Transactional
    public long create(Student student, Date registrationDate) {
        student.setRegistrationDate(registrationDate);
        student.setRegistrationNumber(registrationNumberRepository
                .getNextId(registrationDate, student.getDepartmentCode()));

        validateOrThrow(student);
        save(student);
        return student.getId();
    }

    @Transactional
    public void updateAll(Long id, Student student) {
        validateOrThrow(student);
        student.setId(findOrThrow(id).getId());
        save(student);
    }

    @Transactional
    public void updateProvided(Long id, Student student) {
        Student dbStudent = findOrThrow(id);
        dbStudent.merge(student);
        validateOrThrow(dbStudent);
        save(dbStudent);
    }

    @Transactional
    public void delete(Long id) {
        studentRepository.delete(findOrThrow(id));
    }

    @Transactional
    public void addEducation(Long studentId, Education education) {
        validateOrThrow(education);
        Student student = findWithEducationOrThrow(studentId);
        student.getEducations().add(education);
        save(student);
    }

    @Transactional
    public void deleteEducation(Long studentId, Long educationId) {
        Student student = findWithEducationOrThrow(studentId);
        student.getEducations().remove(findEducationIndexOrThrow(student, educationId));
        save(student);
    }

    @Transactional
    public void updateEducation(Long studentId, Long educationId, Education education) {
        validateOrThrow(education);
        Student student = findWithEducationOrThrow(studentId);

        int idx = student.findEducationById(educationId);
        education.setId(student.getEducations().get(idx).getId());
        student.getEducations().set(idx, education);
        save(student);
    }

    @Transactional
    public void updatePatchEducation(Long studentId, Long educationId, Education education) {
        Student dbStudent = findWithEducationOrThrow(studentId);
        Education dbEducation = dbStudent.getEducations().get(dbStudent.findEducationById(educationId));

        dbEducation.merge(education);
        validateOrThrow(dbEducation);
        save(dbStudent);
    }

    private Student findOrThrow(long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new UmsDataException(ENTITY_NOT_FOUND,
                        FIND_BY_CODE_ERROR_DETAILS_TEMPLATE.formatted(id)));
    }

    private Student findWithEducationOrThrow(long id) {
        return studentRepository.findWithEducationById(id)
                .orElseThrow(() -> new UmsDataException(ENTITY_NOT_FOUND,
                        FIND_BY_CODE_ERROR_DETAILS_TEMPLATE.formatted(id)));
    }

    private int findEducationIndexOrThrow(Student student, long educationId) {
        int index = student.findEducationById(educationId);
        if (index < 0) {
            throw new UmsDataException(ENTITY_NOT_FOUND,
                    FIND_BY_ID_EDUCATION_ERROR_DETAILS_TEMPLATE.formatted(educationId));
        }

        return index;
    }

    private void save(Student student) {
        student.setDepartment(departmentRepository.findByCode(student.getDepartmentCode()).orElseThrow());
        studentRepository.save(student);
    }

    private void validateOrThrow(Student student) {
        BindingResult errors = new BeanPropertyBindingResult(student, "student");
        studentValidator.validate(student, errors);
        exceptionHelper.throwErrorIfValidationError(errors, DATA_VALIDATION_ERROR_DETAILS_TEMPLATE, "department");
    }

    private void validateOrThrow(Education education) {
        BindingResult errors = new BeanPropertyBindingResult(education, "education");
        educationValidator.validate(education, errors);
        exceptionHelper.throwErrorIfValidationError(errors, DATA_VALIDATION_ERROR_EDUCATION_DETAILS_TEMPLATE);
    }
}
