package me.ronygomes.ums.api.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionSystemException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static me.ronygomes.ums.api.helper.DataHelper.*;
import static me.ronygomes.ums.api.helper.TestHelper.extractConstraintViolation;

@SpringBootTest
@ActiveProfiles("integration-test")
public class StudentRepositoryTest {

    private static final String FETCH_EDUCATION_BY_STUDENT_ID_SQL = """
                SELECT * FROM student_educations e
                WHERE e.student_id = :studentId
                ORDER BY idx
            """;

    private static final String FETCH_EXAM_TYPE_BY_EDUCATION_ID_SQL = """
                SELECT %s FROM student_educations e
                WHERE e.id = :id
            """;

    @Autowired
    private StudentRepository repository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EntityManager em;

    @AfterEach
    void tearDown() {
        Assertions.assertEquals(0, repository.findAll().size());
    }

    @Test
    void testStudentRepositoryIsNotNull() {
        Assertions.assertNotNull(repository);
    }

    @Test
    // dbStudent.getEducations() is lazy loaded, fails without it
    @Transactional
    void testCanCreateStudentWithoutEducation() {
        Department cseDepartment = departmentRepository.findByCode("CSE").orElseThrow();
        Student newStudent = validPersistableStudentWithoutEducation(cseDepartment);
        repository.save(newStudent);
        Assertions.assertEquals(1, repository.count());

        Assertions.assertNotNull(newStudent.getId());
        Student dbStudent = repository.findById(newStudent.getId()).orElseThrow();

        assertStudentEquals(newStudent, dbStudent);
        Assertions.assertEquals(0, dbStudent.getEducations().size());

        repository.delete(newStudent);
    }

    @Test
    void testCanCreateStudentWithEducation() {
        Department cseDepartment = departmentRepository.findByCode("CSE").orElseThrow();
        Student newStudent = validPersistableStudent1(cseDepartment);
        repository.save(newStudent);
        Assertions.assertEquals(1, repository.count());

        Assertions.assertNotNull(newStudent.getId());
        Student dbStudent = repository.findWithEducationById(newStudent.getId()).orElseThrow();

        assertStudentEquals(newStudent, dbStudent);
        assertEducationEquals(newStudent.getEducations(), dbStudent.getEducations());

        repository.delete(newStudent);
    }

    @Test
    void testFindByRegistrationNumber() {
        Department cseDepartment = departmentRepository.findByCode("CSE").orElseThrow();
        Student newStudent = validPersistableStudent1(cseDepartment);
        repository.save(newStudent);

        Assertions.assertNotNull(newStudent.getId());
        Student dbStudent = repository.findByRegistrationNumber(newStudent.getRegistrationNumber()).orElseThrow();

        assertStudentEquals(newStudent, dbStudent);
        assertEducationEquals(newStudent.getEducations(), dbStudent.getEducations());

        repository.delete(newStudent);
    }

    @Test
    void testDeleteStudent() {
        Department cseDepartment = departmentRepository.findByCode("CSE").orElseThrow();
        Student newStudent = validPersistableStudent1(cseDepartment);
        repository.save(newStudent);

        List<Education> dbEducations = findAllEducationById(newStudent.getId());
        assertEducationEquals(newStudent.getEducations(), dbEducations);

        repository.delete(newStudent);

        dbEducations = findAllEducationById(newStudent.getId());
        Assertions.assertEquals(0, dbEducations.size());
    }

    @Test
    @Transactional
    void testUpdateStudent() {
        Department cseDepartment = departmentRepository.findByCode("CSE").orElseThrow();
        Student student = validPersistableStudent1(cseDepartment);
        repository.save(student);

        Student dbStudent = repository.findWithEducationById(student.getId()).orElseThrow();
        assertStudentEquals(dbStudent, student);
        assertEducationEquals(dbStudent.getEducations(), student.getEducations());

        // Won't update id, can't update registration number, certificateFileName, certificateFilePath (updatable=false)
        student.setFullName("Updated Full Name");
        student.setEmail("updated@email.com");
        student.setContactNumber("+5501327938765");
        student.setAddress("Updated Address");
        student.setDepartment(departmentRepository.findByCode("EEE").orElseThrow());
        student.setRegistrationDate(Date.from(Instant.now().minus(Duration.ofDays(10))));

        // Can't update certificateFileName, certificateFilePath (updatable=false)
        Education education = new Education();
        education.setExamType(ExamType.A_LEVEL);
        education.setGrade(Grade.C);
        education.setCgpa(Grade.C.getGpa());
        education.setCertificateFileName("a-level-certificate.pdf");
        education.setCertificatePath("some-random-path/2024-CSE-0001/a-level-certificate.pdf");

        // Commented code fails with JpaSystemException as it overrides Hibernate Proxy
        // student.setEducations(new ArrayList<>(List.of(education)));
        student.getEducations().clear();
        student.getEducations().add(education);

        repository.save(student);
        Student updatedStudent = repository.findWithEducationById(student.getId()).orElseThrow();

        Assertions.assertEquals(student.getId(), updatedStudent.getId());
        assertStudentEquals(updatedStudent, student);
        assertEducationEquals(updatedStudent.getEducations(), student.getEducations());
        Assertions.assertEquals(1, findAllEducationById(student.getId()).size());

        repository.delete(student);
    }

    @Test
    void testFullNameConstrains() {
        Department cseDepartment = departmentRepository.findByCode("CSE").orElseThrow();
        Student student = validPersistableStudent1(cseDepartment);

        student.setFullName(null);
        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(student));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("fullName", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        student.setFullName("a".repeat(201));
        Throwable exMaxLength = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(student));
        Set<ConstraintViolation<?>> maxLenViolations = extractConstraintViolation(exMaxLength);
        Assertions.assertEquals(1, maxLenViolations.size());
        maxLenViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 200", v.getMessage());
            Assertions.assertEquals("fullName", v.getPropertyPath().toString());
            Assertions.assertEquals(201, v.getInvalidValue().toString().length());
        });

        student.setFullName("");
        Throwable exMinLength = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(student));
        Set<ConstraintViolation<?>> minLenViolations = extractConstraintViolation(exMinLength);
        Assertions.assertEquals(1, minLenViolations.size());
        minLenViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 200", v.getMessage());
            Assertions.assertEquals("fullName", v.getPropertyPath().toString());
            Assertions.assertEquals(0, v.getInvalidValue().toString().length());
        });
    }

    @Test
    void testStudentEmailFieldConstraints() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Student nullEmail = DataHelper.validPersistableStudent1(department);
        nullEmail.setEmail(null);
        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullEmail));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("email", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Student minLength = DataHelper.validPersistableStudent1(department);
        minLength.setEmail("a@b");
        Throwable exMinLen = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(minLength));
        Set<ConstraintViolation<?>> minLenViolations = extractConstraintViolation(exMinLen);
        Assertions.assertEquals(2, minLenViolations.size());
        minLenViolations.forEach(v -> {
            if (v.getMessage().equals("size must be between 5 and 100")) {
                Assertions.assertEquals("email", v.getPropertyPath().toString());
                Assertions.assertEquals(3, v.getInvalidValue().toString().length());
            }
        });

        Student maxLength = DataHelper.validPersistableStudent1(department);
        maxLength.setEmail("a".repeat(91) + "@gmail.com");
        Throwable exMaxLen = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxLength));
        Set<ConstraintViolation<?>> maxLengthViolations = extractConstraintViolation(exMaxLen);
        Assertions.assertEquals(1, maxLengthViolations.size());
        maxLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 5 and 100", v.getMessage());
            Assertions.assertEquals("email", v.getPropertyPath().toString());
            Assertions.assertEquals(101, v.getInvalidValue().toString().length());
        });

        Student pattern = DataHelper.validPersistableStudent1(department);
        pattern.setEmail("a".repeat(100));
        Throwable exceptionInvalidEmail = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(pattern));
        Set<ConstraintViolation<?>> invalidPatternViolations = extractConstraintViolation(exceptionInvalidEmail);
        Assertions.assertEquals(1, invalidPatternViolations.size());
        invalidPatternViolations.forEach(v -> {
            Assertions.assertEquals("invalid email format", v.getMessage());
            Assertions.assertEquals("email", v.getPropertyPath().toString());
            Assertions.assertEquals(100, v.getInvalidValue().toString().length());
        });

        Student student1 = DataHelper.validPersistableStudent1(department);
        Student student2 = DataHelper.validPersistableStudent2(department);
        student2.setEmail(student1.getEmail());

        repository.save(student1);
        Assertions.assertEquals(1, repository.findAll().size());
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(student2));

        Assertions.assertNotNull(student2.getId());
        Assertions.assertTrue(repository.findById(student2.getId()).isEmpty());

        repository.deleteById(student1.getId());
    }

    @Test
    void testStudentContactNumberFieldConstraints() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Student nullContact = DataHelper.validPersistableStudent1(department);
        nullContact.setContactNumber(null);
        repository.save(nullContact);

        Assertions.assertNull(repository.findById(nullContact.getId())
                .orElseThrow(IllegalStateException::new).getContactNumber());

        repository.deleteById(nullContact.getId());

        Student maxLenContact = DataHelper.validPersistableStudent1(department);
        maxLenContact.setContactNumber(maxLenContact.getContactNumber() + "2");
        Throwable exceptionMaxLengthContactNumber = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxLenContact));
        Set<ConstraintViolation<?>> exceptionMaxLengthContactNumberViolations = extractConstraintViolation(exceptionMaxLengthContactNumber);
        Assertions.assertEquals(2, exceptionMaxLengthContactNumberViolations.size());
        exceptionMaxLengthContactNumberViolations.forEach(v -> {
            Assertions.assertTrue(Arrays.asList("size must be between 0 and 14", "invalid contact number format").contains(v.getMessage()));
            Assertions.assertEquals("contactNumber", v.getPropertyPath().toString());
            Assertions.assertEquals(15, v.getInvalidValue().toString().length());
        });
    }

    @Test
    void testStudentAddressFieldConstraints() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Student nullAddress = DataHelper.validPersistableStudent1(department);
        nullAddress.setAddress(null);
        repository.save(nullAddress);
        Assertions.assertNull(repository.findById(nullAddress.getId()).orElseThrow().getAddress());
        repository.deleteById(nullAddress.getId());

        Student maxLenAddress = DataHelper.validPersistableStudent1(department);
        maxLenAddress.setAddress("A".repeat(1001));
        Throwable exceptionMaxLengthTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxLenAddress));
        Set<ConstraintViolation<?>> exceptionMaxLengthViolations = extractConstraintViolation(exceptionMaxLengthTest);
        Assertions.assertEquals(1, exceptionMaxLengthViolations.size());
        exceptionMaxLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 0 and 1000", v.getMessage());
            Assertions.assertEquals("address", v.getPropertyPath().toString());
            Assertions.assertEquals("A".repeat(1001), v.getInvalidValue());
        });
    }

    @Test
    void testStudentDepartmentFieldConstraints() {

        Student studentWithNullDepartment = DataHelper.validPersistableStudent1(null);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(studentWithNullDepartment));

        Department d = new Department();
        d.setId(500L);

        Student studentWithNonExistentDepartment = DataHelper.validPersistableStudent1(d);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(studentWithNonExistentDepartment));
    }

    @Test
    void testRegistrationDateConstrains() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Student nullRegDate = DataHelper.validPersistableStudent1(department);
        nullRegDate.setRegistrationDate(null);
        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullRegDate));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("registrationDate", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Student futureRegDate = DataHelper.validPersistableStudent1(department);
        Date futureDate = Date.from(Instant.now().plus(Duration.ofDays(1)));
        futureRegDate.setRegistrationDate(futureDate);
        Throwable exFuture = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(futureRegDate));
        Set<ConstraintViolation<?>> futureViolations = extractConstraintViolation(exFuture);
        Assertions.assertEquals(1, futureViolations.size());
        futureViolations.forEach(v -> {
            Assertions.assertEquals("must be a date in the past or in the present", v.getMessage());
            Assertions.assertEquals("registrationDate", v.getPropertyPath().toString());
            Assertions.assertEquals(0, futureDate.compareTo((Date) v.getInvalidValue()));
        });
    }

    @Test
    void testRegistrationNumberConstrains() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Student nullRegNum = DataHelper.validPersistableStudent1(department);
        nullRegNum.setRegistrationNumber(null);
        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullRegNum));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("registrationNumber", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Student invalidPattern = DataHelper.validPersistableStudent1(department);
        invalidPattern.setRegistrationNumber("123-ABC-1230");
        Throwable exPattern = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(invalidPattern));
        Set<ConstraintViolation<?>> patternViolations = extractConstraintViolation(exPattern);
        Assertions.assertEquals(1, patternViolations.size());
        patternViolations.forEach(v -> {
            Assertions.assertEquals("invalid registration number", v.getMessage());
            Assertions.assertEquals("registrationNumber", v.getPropertyPath().toString());
            Assertions.assertEquals(invalidPattern.getRegistrationNumber(), v.getInvalidValue());
        });

        Student student1 = DataHelper.validPersistableStudent1(department);
        Student student2 = DataHelper.validPersistableStudent2(department);
        student2.setRegistrationNumber(student1.getRegistrationNumber());

        repository.save(student1);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(student2));
        repository.delete(student1);
    }

    @Test
    void testConstrainsEducation_examType() {
        for (ExamType et : ExamType.values()) {
            Assertions.assertTrue(et.name().length() <= 10, String.format("'%s' length is greater than 10", et.name()));
        }

        Department department = departmentRepository.findByCode("CSE").orElseThrow();
        Student nullExamType = DataHelper.validPersistableStudent1(department);
        Assertions.assertEquals(2, nullExamType.getEducations().size());

        nullExamType.getEducations().get(0).setExamType(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullExamType));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("examType", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Student student = DataHelper.validPersistableStudent1(department);
        student.getEducations().remove(0);
        Assertions.assertEquals(1, student.getEducations().size());

        repository.save(student);

        Assertions.assertEquals(student.getEducations().get(0).getExamType().name(),
                findEducationColumnDataById(student.getEducations().get(0).getId(), ExamType.class));

        repository.delete(student);
    }

    @Test
    void testConstrainsEducation_grade() {
        for (Grade g : Grade.values()) {
            Assertions.assertTrue(g.name().length() <= 10, String.format("'%s' length is greater than 10", g.name()));
        }

        Department department = departmentRepository.findByCode("CSE").orElseThrow();
        Student nullGrade = DataHelper.validPersistableStudent1(department);
        Assertions.assertEquals(2, nullGrade.getEducations().size());

        nullGrade.getEducations().get(0).setGrade(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullGrade));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("grade", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Student student = DataHelper.validPersistableStudent1(department);
        student.getEducations().remove(0);
        Assertions.assertEquals(1, student.getEducations().size());

        repository.save(student);

        Assertions.assertEquals(student.getEducations().get(0).getGrade().name(),
                findEducationColumnDataById(student.getEducations().get(0).getId(), Grade.class));

        repository.delete(student);
    }

    @Test
    void testConstrainsEducation_cgpa() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Assertions.assertEquals(0.0, new Education().getCgpa());

        Student minCgpa = validPersistableStudentWithoutEducation(department);
        Education e = educationWithGrade(-1.0f);
        minCgpa.getEducations().add(e);

        Throwable exMin = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(minCgpa));
        Set<ConstraintViolation<?>> minViolations = extractConstraintViolation(exMin);
        Assertions.assertEquals(1, minViolations.size());
        minViolations.forEach(v -> {
            Assertions.assertEquals("must be greater than or equal to 0.0", v.getMessage());
            Assertions.assertEquals("cgpa", v.getPropertyPath().toString());
            Assertions.assertEquals(-1.0f, ((BigDecimal) v.getInvalidValue()).floatValue());
        });

        Student maxCgpa = validPersistableStudentWithoutEducation(department);
        e = educationWithGrade(5.1f);
        maxCgpa.getEducations().add(e);

        Throwable exMax = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxCgpa));
        Set<ConstraintViolation<?>> maxViolations = extractConstraintViolation(exMax);
        Assertions.assertEquals(1, maxViolations.size());
        maxViolations.forEach(v -> {
            Assertions.assertEquals("must be less than or equal to 5.0", v.getMessage());
            Assertions.assertEquals("cgpa", v.getPropertyPath().toString());
            Assertions.assertEquals(0, Float.compare(5.1f, ((BigDecimal) v.getInvalidValue()).floatValue()));
        });
    }

    @Test
    void testConstrainsEducation_certificateFileName() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();
        Student nullFileName = validPersistableStudentWithoutEducation(department);
        Education e = educationWithGrade(4.0f);
        e.setCertificateFileName(null);
        nullFileName.getEducations().add(e);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullFileName));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("certificateFileName", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Student minFilename = validPersistableStudentWithoutEducation(department);
        e = educationWithGrade(4.0f);
        e.setCertificateFileName("");
        minFilename.getEducations().add(e);

        Throwable exMix = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(minFilename));
        Set<ConstraintViolation<?>> minViolations = extractConstraintViolation(exMix);
        minViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 100", v.getMessage());
            Assertions.assertEquals("certificateFileName", v.getPropertyPath().toString());
            Assertions.assertEquals("", v.getInvalidValue());
        });

        Student maxFilename = validPersistableStudentWithoutEducation(department);
        e = educationWithGrade(4.0f);
        e.setCertificateFileName("a".repeat(101));
        maxFilename.getEducations().add(e);

        Throwable exMax = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxFilename));
        Set<ConstraintViolation<?>> maxViolations = extractConstraintViolation(exMax);
        maxViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 100", v.getMessage());
            Assertions.assertEquals("certificateFileName", v.getPropertyPath().toString());
            Assertions.assertEquals("a".repeat(101), v.getInvalidValue());
        });

        Student valid = validPersistableStudentWithoutEducation(department);
        e = educationWithGrade(4.0f);
        valid.getEducations().add(e);

        repository.save(valid);
        Assertions.assertEquals(1, repository.findAll().size());

        Student dbStudent = repository.findWithEducationById(valid.getId()).orElseThrow();
        assertEducationEquals(List.of(e), dbStudent.getEducations());

        String oldFilename = dbStudent.getEducations().get(0).getCertificateFileName();
        dbStudent.getEducations().get(0).setCertificateFileName("updated");
        repository.save(dbStudent);

        dbStudent = repository.findWithEducationById(valid.getId()).orElseThrow();
        Assertions.assertEquals(oldFilename, dbStudent.getEducations().get(0).getCertificateFileName());

        repository.delete(valid);
    }

    @Test
    void testConstrainsEducation_certificatePath() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();
        Student nullFileName = validPersistableStudentWithoutEducation(department);
        Education e = educationWithGrade(4.0f);
        e.setCertificatePath(null);
        nullFileName.getEducations().add(e);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullFileName));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("certificatePath", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Student minFilename = validPersistableStudentWithoutEducation(department);
        e = educationWithGrade(4.0f);
        e.setCertificatePath("");
        minFilename.getEducations().add(e);

        Throwable exMix = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(minFilename));
        Set<ConstraintViolation<?>> minViolations = extractConstraintViolation(exMix);
        minViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 150", v.getMessage());
            Assertions.assertEquals("certificatePath", v.getPropertyPath().toString());
            Assertions.assertEquals("", v.getInvalidValue());
        });

        Student maxFilename = validPersistableStudentWithoutEducation(department);
        e = educationWithGrade(4.0f);
        e.setCertificatePath("a".repeat(151));
        maxFilename.getEducations().add(e);

        Throwable exMax = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxFilename));
        Set<ConstraintViolation<?>> maxViolations = extractConstraintViolation(exMax);
        maxViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 150", v.getMessage());
            Assertions.assertEquals("certificatePath", v.getPropertyPath().toString());
            Assertions.assertEquals("a".repeat(151), v.getInvalidValue());
        });

        Student valid = validPersistableStudentWithoutEducation(department);
        e = educationWithGrade(4.0f);
        valid.getEducations().add(e);

        repository.save(valid);
        Assertions.assertEquals(1, repository.findAll().size());

        Student dbStudent = repository.findWithEducationById(valid.getId()).orElseThrow();
        assertEducationEquals(List.of(e), dbStudent.getEducations());

        String oldPath = dbStudent.getEducations().get(0).getCertificatePath();
        dbStudent.getEducations().get(0).setCertificatePath("updated");
        repository.save(dbStudent);

        dbStudent = repository.findWithEducationById(valid.getId()).orElseThrow();
        Assertions.assertEquals(oldPath, dbStudent.getEducations().get(0).getCertificatePath());

        repository.delete(valid);
    }

    private void assertStudentEquals(Student student1, Student student2) {
        Assertions.assertEquals(student1.getFullName(), student2.getFullName());
        Assertions.assertEquals(student1.getAddress(), student2.getAddress());
        Assertions.assertEquals(student1.getEmail(), student2.getEmail());
        Assertions.assertEquals(student1.getContactNumber(), student2.getContactNumber());
        Assertions.assertEquals(student1.getDepartment(), student2.getDepartment());
        Assertions.assertEquals(0, student1.getRegistrationDate().compareTo(student2.getRegistrationDate()));
        Assertions.assertEquals(student1.getRegistrationNumber(), student2.getRegistrationNumber());
    }

    private void assertEducationEquals(List<Education> educations1, List<Education> educations2) {
        Assertions.assertEquals(educations1.size(), educations2.size());

        for (int i = 0; i < educations1.size(); i++) {
            Education expected = educations1.get(i);
            Assertions.assertNotNull(expected.getId());
            Assertions.assertEquals(expected, educations2.get(i));
        }
    }

    private List<Education> findAllEducationById(Long studentId) {
        List<Education> educations = (List<Education>) em.createNativeQuery(FETCH_EDUCATION_BY_STUDENT_ID_SQL, Education.class)
                .setParameter("studentId", studentId)
                .getResultList();

        return educations;
    }

    private <E extends Enum<E>> String findEducationColumnDataById(Long educationId, Class<E> type) {
        String columnName = "";
        if (type.isAssignableFrom(ExamType.class)) {
            columnName = "exam_type";
        } else if (type.isAssignableFrom(Grade.class)) {
            columnName = "grade";
        } else {
            throw new IllegalStateException();
        }

        return (String) em.createNativeQuery(FETCH_EXAM_TYPE_BY_EDUCATION_ID_SQL.formatted(columnName), String.class)
                .setParameter("id", educationId).getSingleResult();
    }
}
