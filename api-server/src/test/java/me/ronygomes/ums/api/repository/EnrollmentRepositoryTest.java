package me.ronygomes.ums.api.repository;

import jakarta.validation.ConstraintViolation;
import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionSystemException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import static me.ronygomes.ums.api.helper.DataHelper.validPersistableEnrollment1;
import static me.ronygomes.ums.api.helper.TestHelper.extractConstraintViolation;
import static me.ronygomes.ums.api.helper.TestHelper.isEnumFieldStoredAsString;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers(disabledWithoutDocker = true)
public class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository repository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Course course;
    private Student student;

    @BeforeEach
    void setup() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();
        course = DataHelper.validPersistableCourse1(department, null);
        student = DataHelper.validPersistableStudent1(department);

        courseRepository.save(course);
        studentRepository.save(student);
    }

    @AfterEach
    void tearDown() {
        courseRepository.delete(course);
        studentRepository.delete(student);

        Assertions.assertEquals(0, repository.findAll().size());
    }

    @Test
    void testSave() {
        Enrollment enrollment = validPersistableEnrollment1(student, course);

        repository.save(enrollment);

        Enrollment dbEnrollment = repository.findById(enrollment.getId()).orElseThrow();
        assertEnrolmentEquals(enrollment, dbEnrollment);

        repository.delete(enrollment);
    }

    @Test
    void testUpdate() {
        Enrollment enrollment = validPersistableEnrollment1(student, course);

        repository.save(enrollment);

        Enrollment updatedEnrollment = repository.findById(enrollment.getId()).orElseThrow();
        updatedEnrollment.setStatus(EnrollmentStatus.PASSED);
        updatedEnrollment.setGrade(Grade.F);

        Date yesterday = Date.from(Instant.now().minus(Duration.ofDays(1)));
        updatedEnrollment.setEnrollmentDate(yesterday);

        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        Course course2 = DataHelper.validPersistableCourse2(department, null);
        Student student2 = DataHelper.validPersistableStudent2(department);

        studentRepository.save(student2);
        courseRepository.save(course2);

        updatedEnrollment.setCourse(course2);
        updatedEnrollment.setStudent(student2);

        repository.save(updatedEnrollment);

        Enrollment dbEnrollment = repository.findById(updatedEnrollment.getId()).orElseThrow();
        assertEnrolmentEquals(updatedEnrollment, dbEnrollment);

        repository.delete(dbEnrollment);
        studentRepository.delete(student2);
        courseRepository.delete(course2);
    }

    @Test
    void testFieldConstrainStudent() {
        Enrollment nullField = validPersistableEnrollment1(null, course);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullField));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("student", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Student s = new Student();
        s.setId(500L);

        Enrollment invalidRefField = validPersistableEnrollment1(s, course);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(invalidRefField));
    }

    @Test
    void testFieldConstrainCourse() {
        Enrollment nullField = validPersistableEnrollment1(student, null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullField));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("course", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Course c = new Course();
        c.setId(500L);

        Enrollment invalidRefField = validPersistableEnrollment1(student, c);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(invalidRefField));
    }

    @Test
    void testFieldConstrainEnrollmentDate() {
        Enrollment nullField = validPersistableEnrollment1(student, course);
        nullField.setEnrollmentDate(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullField));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("enrollmentDate", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Enrollment futureEnrollDate = validPersistableEnrollment1(student, course);
        Date futureDate = Date.from(Instant.now().plus(Duration.ofDays(1)));
        futureEnrollDate.setEnrollmentDate(futureDate);
        Throwable exFuture = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(futureEnrollDate));
        Set<ConstraintViolation<?>> futureViolations = extractConstraintViolation(exFuture);
        Assertions.assertEquals(1, futureViolations.size());
        futureViolations.forEach(v -> {
            Assertions.assertEquals("must be a date in the past or in the present", v.getMessage());
            Assertions.assertEquals("enrollmentDate", v.getPropertyPath().toString());
            Assertions.assertEquals(0, futureDate.compareTo((Date) v.getInvalidValue()));
        });
    }

    @Test
    void testFieldConstrainStatus() {
        Assertions.assertTrue(isEnumFieldStoredAsString(Enrollment.class, "status"));
        for (EnrollmentStatus e : EnrollmentStatus.values()) {
            Assertions.assertTrue(e.name().length() <= 10);
        }

        Enrollment enrollment = validPersistableEnrollment1(student, course);
        enrollment.setStatus(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(enrollment));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("status", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });
    }

    @Test
    void testFieldConstrainGrade() {
        Assertions.assertTrue(isEnumFieldStoredAsString(Enrollment.class, "grade"));
        for (Grade g : Grade.values()) {
            Assertions.assertTrue(g.name().length() <= 10);
        }

        Enrollment enrollment = validPersistableEnrollment1(student, course);
        enrollment.setGrade(null);

        Assertions.assertDoesNotThrow(() -> repository.save(enrollment));
        repository.delete(enrollment);
    }

    private void assertEnrolmentEquals(Enrollment enrollment1, Enrollment enrollment2) {
        Assertions.assertEquals(enrollment1.getStudent(), enrollment2.getStudent());
        Assertions.assertEquals(enrollment1.getCourse(), enrollment2.getCourse());
        Assertions.assertEquals(0, enrollment1.getEnrollmentDate().compareTo(enrollment2.getEnrollmentDate()));
        Assertions.assertEquals(enrollment1.getStatus(), enrollment2.getStatus());
        Assertions.assertEquals(enrollment1.getGrade(), enrollment2.getGrade());
    }
}
