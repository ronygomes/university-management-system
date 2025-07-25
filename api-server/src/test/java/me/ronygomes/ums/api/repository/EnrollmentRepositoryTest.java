package me.ronygomes.ums.api.repository;

import jakarta.validation.ConstraintViolation;
import me.ronygomes.ums.api.model.*;
import me.ronygomes.ums.api.testHelper.DataHelper;
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

import static me.ronygomes.ums.api.testHelper.DataHelper.validPersistableEnrollment1;
import static me.ronygomes.ums.api.testHelper.TestHelper.extractConstraintViolation;
import static me.ronygomes.ums.api.testHelper.TestHelper.isEnumFieldStoredAsString;

@SpringBootTest
@ActiveProfiles("database-test")
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

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    private Course course;
    private Student student;
    private Department department;
    private CourseSchedule courseSchedule;

    @BeforeEach
    void setup() {
        department = departmentRepository.findByCode("CSE").orElseThrow();
        course = DataHelper.validPersistableCourse1(department, null);
        student = DataHelper.validPersistableStudent1(department);
        courseSchedule = DataHelper.validPersistableCourseSchedule1(department, course);

        courseRepository.save(course);
        studentRepository.save(student);
        courseScheduleRepository.save(courseSchedule);
    }

    @AfterEach
    void tearDown() {
        courseScheduleRepository.delete(courseSchedule);
        courseRepository.delete(course);
        studentRepository.delete(student);

        Assertions.assertEquals(0, repository.findAll().size());
    }

    @Test
    void testSave() {
        Enrollment enrollment = validPersistableEnrollment1(student, courseSchedule);

        repository.save(enrollment);

        Enrollment dbEnrollment = repository.findById(enrollment.getId()).orElseThrow();
        assertEnrolmentEquals(enrollment, dbEnrollment);

        repository.delete(enrollment);
    }

    @Test
    void testUpdate() {
        Enrollment enrollment = validPersistableEnrollment1(student, courseSchedule);

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
        CourseSchedule courseSchedule2 = DataHelper.validPersistableCourseSchedule2(department, course2);
        courseScheduleRepository.save(courseSchedule2);

        updatedEnrollment.setCourseSchedule(courseSchedule2);
        updatedEnrollment.setStudent(student2);

        repository.save(updatedEnrollment);

        Enrollment dbEnrollment = repository.findById(updatedEnrollment.getId()).orElseThrow();
        assertEnrolmentEquals(updatedEnrollment, dbEnrollment);

        repository.delete(dbEnrollment);
        studentRepository.delete(student2);
        courseScheduleRepository.delete(courseSchedule2);
        courseRepository.delete(course2);
    }

    @Test
    void testFieldConstrainStudent() {
        Enrollment nullField = validPersistableEnrollment1(null, courseSchedule);

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

        Enrollment invalidRefField = validPersistableEnrollment1(s, courseSchedule);
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
            Assertions.assertEquals("courseSchedule", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        CourseSchedule cs = new CourseSchedule();
        cs.setId(500L);

        Enrollment invalidRefField = validPersistableEnrollment1(student, cs);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(invalidRefField));
    }

    @Test
    void testFieldConstrainEnrollmentDate() {
        Enrollment nullField = validPersistableEnrollment1(student, courseSchedule);
        nullField.setEnrollmentDate(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullField));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("enrollmentDate", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Enrollment futureEnrollDate = validPersistableEnrollment1(student, courseSchedule);
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

        Enrollment enrollment = validPersistableEnrollment1(student, courseSchedule);
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

        Enrollment enrollment = validPersistableEnrollment1(student, courseSchedule);
        enrollment.setGrade(null);

        Assertions.assertDoesNotThrow(() -> repository.save(enrollment));
        repository.delete(enrollment);
    }

    private void assertEnrolmentEquals(Enrollment enrollment1, Enrollment enrollment2) {
        Assertions.assertEquals(enrollment1.getStudent(), enrollment2.getStudent());
        Assertions.assertEquals(enrollment1.getCourseSchedule(), enrollment2.getCourseSchedule());
        Assertions.assertEquals(0, enrollment1.getEnrollmentDate().compareTo(enrollment2.getEnrollmentDate()));
        Assertions.assertEquals(enrollment1.getStatus(), enrollment2.getStatus());
        Assertions.assertEquals(enrollment1.getGrade(), enrollment2.getGrade());
    }
}
