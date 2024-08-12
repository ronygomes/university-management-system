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

import java.math.BigDecimal;
import java.util.Set;

import static me.ronygomes.ums.api.helper.TestHelper.extractConstraintViolation;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers(disabledWithoutDocker = true)
public class CourseRepositoryTest {

    @Autowired
    private CourseRepository repository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private DesignationRepository designationRepository;

    private Teacher teacher;

    @BeforeEach
    void setup() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();
        Designation designation = designationRepository.findByTitle("Lecturer").orElseThrow();

        teacher = DataHelper.validPersistableTeacher1(designation, department);
        teacherRepository.save(teacher);
    }

    @AfterEach
    void tearDown() {
        Assertions.assertEquals(0, repository.findAll().size());
        teacherRepository.delete(teacher);
        Assertions.assertEquals(0, teacherRepository.findAll().size());
    }

    @Test
    void testDepartmentRepositoryIsNotNull() {
        Assertions.assertNotNull(repository);
    }

    @Test
    void testSave() {
        Department d = departmentRepository.findByCode("CSE").orElseThrow();
        Course course = DataHelper.validPersistableCourse1(d, teacher);

        repository.save(course);

        Course dbCourse = repository.findById(course.getId()).orElseThrow();
        assertCourseEqual(course, dbCourse);

        repository.delete(course);

        Course courseWithoutTeacher = DataHelper.validPersistableCourse1(d, null);
        Assertions.assertDoesNotThrow(() -> repository.save(courseWithoutTeacher));
        repository.delete(courseWithoutTeacher);
    }

    @Test
    void testUpdate() {
        Department d1 = departmentRepository.findByCode("CSE").orElseThrow();
        Course course = DataHelper.validPersistableCourse1(d1, teacher);

        repository.save(course);
        Long courseId = course.getId();
        Assertions.assertEquals(1, repository.findAll().size());

        Department d2 = departmentRepository.findByCode("EEE").orElseThrow();
        Designation designation = designationRepository.findByTitle("Professor").orElseThrow();
        Teacher otherTeacher = DataHelper.validPersistableTeacher2(designation, d2);
        teacherRepository.save(otherTeacher);

        Course updatedCourse = DataHelper.validPersistableCourse2(d2, otherTeacher);
        updatedCourse.setId(course.getId());
        updatedCourse.setUuid(course.getUuid());
        updatedCourse.setVersion(course.getVersion());

        repository.save(updatedCourse);

        Course dbCourse = repository.findById(courseId).orElseThrow();
        assertCourseEqual(updatedCourse, dbCourse);

        repository.delete(dbCourse);
        teacherRepository.delete(otherTeacher);
    }

    @Test
    void testFieldConstrainsTitle() {
        Department d1 = departmentRepository.findByCode("CSE").orElseThrow();

        Course nullCourse = DataHelper.validPersistableCourse1(d1, teacher);
        nullCourse.setTitle(null);
        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullCourse));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("title", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Course minLen = DataHelper.validPersistableCourse1(d1, teacher);
        minLen.setTitle("");
        Throwable exMinLength = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(minLen));
        Set<ConstraintViolation<?>> minLenViolations = extractConstraintViolation(exMinLength);
        Assertions.assertEquals(1, minLenViolations.size());
        minLenViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 20", v.getMessage());
            Assertions.assertEquals("title", v.getPropertyPath().toString());
            Assertions.assertEquals(0, v.getInvalidValue().toString().length());
        });

        Course maxLen = DataHelper.validPersistableCourse1(d1, teacher);
        maxLen.setTitle("a".repeat(21));
        Throwable exMaxLength = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxLen));
        Set<ConstraintViolation<?>> maxLenViolations = extractConstraintViolation(exMaxLength);
        Assertions.assertEquals(1, maxLenViolations.size());
        maxLenViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 20", v.getMessage());
            Assertions.assertEquals("title", v.getPropertyPath().toString());
            Assertions.assertEquals("a".repeat(21), v.getInvalidValue().toString());
        });

        Course valid1 = DataHelper.validPersistableCourse1(d1, teacher);
        repository.save(valid1);

        Course valid2 = DataHelper.validPersistableCourse1(d1, teacher);
        valid2.setTitle(valid1.getTitle());
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(valid2));

        repository.delete(valid1);
    }

    @Test
    void testFieldConstrainsName() {
        Department d1 = departmentRepository.findByCode("CSE").orElseThrow();

        Course nullCourse = DataHelper.validPersistableCourse1(d1, teacher);
        nullCourse.setName(null);
        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullCourse));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("name", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Course minLen = DataHelper.validPersistableCourse1(d1, teacher);
        minLen.setName("");
        Throwable exMinLength = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(minLen));
        Set<ConstraintViolation<?>> minLenViolations = extractConstraintViolation(exMinLength);
        Assertions.assertEquals(1, minLenViolations.size());
        minLenViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 200", v.getMessage());
            Assertions.assertEquals("name", v.getPropertyPath().toString());
            Assertions.assertEquals(0, v.getInvalidValue().toString().length());
        });

        Course maxLen = DataHelper.validPersistableCourse1(d1, teacher);
        maxLen.setName("a".repeat(201));
        Throwable exMaxLength = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxLen));
        Set<ConstraintViolation<?>> maxLenViolations = extractConstraintViolation(exMaxLength);
        Assertions.assertEquals(1, maxLenViolations.size());
        maxLenViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 200", v.getMessage());
            Assertions.assertEquals("name", v.getPropertyPath().toString());
            Assertions.assertEquals("a".repeat(201), v.getInvalidValue().toString());
        });

        Course valid1 = DataHelper.validPersistableCourse1(d1, teacher);
        repository.save(valid1);

        Course valid2 = DataHelper.validPersistableCourse1(d1, teacher);
        valid2.setName(valid1.getTitle());
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(valid2));

        repository.delete(valid1);
    }

    @Test
    void testFieldConstrainsCredit() {
        Assertions.assertEquals(0.0, new Course().getCredit());

        Department d1 = departmentRepository.findByCode("CSE").orElseThrow();
        Course minCredit = DataHelper.validPersistableCourse1(d1, teacher);
        minCredit.setCredit(-0.1f);

        Throwable exMin = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(minCredit));
        Set<ConstraintViolation<?>> minViolations = extractConstraintViolation(exMin);
        Assertions.assertEquals(1, minViolations.size());
        minViolations.forEach(v -> {
            Assertions.assertEquals("must be greater than or equal to 0.0", v.getMessage());
            Assertions.assertEquals("credit", v.getPropertyPath().toString());
            Assertions.assertEquals(-0.1f, ((BigDecimal) v.getInvalidValue()).floatValue());
        });

        Course maxCredit = DataHelper.validPersistableCourse1(d1, teacher);
        maxCredit.setCredit(5.1f);
        Throwable exMax = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxCredit));
        Set<ConstraintViolation<?>> maxViolations = extractConstraintViolation(exMax);
        Assertions.assertEquals(1, maxViolations.size());
        maxViolations.forEach(v -> {
            Assertions.assertEquals("must be less than or equal to 5.0", v.getMessage());
            Assertions.assertEquals("credit", v.getPropertyPath().toString());
            Assertions.assertEquals(0, Float.compare(5.1f, ((BigDecimal) v.getInvalidValue()).floatValue()));
        });
    }

    @Test
    void testFieldConstrainsDescription() {
        Assertions.assertEquals(0.0, new Course().getCredit());
        Department d1 = departmentRepository.findByCode("CSE").orElseThrow();
        Course nullDescription = DataHelper.validPersistableCourse1(d1, teacher);
        nullDescription.setDescription(null);

        repository.save(nullDescription);
        assertCourseEqual(nullDescription, repository.findById(nullDescription.getId()).orElseThrow());
        repository.delete(nullDescription);

        Course maxDescription = DataHelper.validPersistableCourse1(d1, teacher);
        maxDescription.setDescription("a".repeat(2001));
        Throwable exMax = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxDescription));
        Set<ConstraintViolation<?>> maxViolations = extractConstraintViolation(exMax);
        Assertions.assertEquals(1, maxViolations.size());
        maxViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 0 and 2000", v.getMessage());
            Assertions.assertEquals("description", v.getPropertyPath().toString());
            Assertions.assertEquals("a".repeat(2001), v.getInvalidValue());
        });

        repository.delete(maxDescription);
    }

    @Test
    void testFieldConstrainsDepartment() {
        Course nullDepartment = DataHelper.validPersistableCourse1(null, teacher);
        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullDepartment));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("department", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Department d = new Department();
        d.setId(500L);

        Course nonExistentDepartment = DataHelper.validPersistableCourse1(d, teacher);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(nonExistentDepartment));
    }

    @Test
    void testFieldConstrainsSemester() {
        for (Semester s : Semester.values()) {
            Assertions.assertTrue(s.name().length() <= 30, String.format("'%s' length is greater than 30", s.name()));
        }

        Department d1 = departmentRepository.findByCode("CSE").orElseThrow();
        Course nullSemester = DataHelper.validPersistableCourse1(d1, teacher);
        nullSemester.setSemester(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullSemester));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("semester", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });
    }

    @Test
    void testFieldConstrainsInstructor() {
        Department d1 = departmentRepository.findByCode("CSE").orElseThrow();
        Course nullTeacher = DataHelper.validPersistableCourse1(d1, null);

        repository.save(nullTeacher);
        Assertions.assertNull(repository.findById(nullTeacher.getId()).orElseThrow().getInstructor());
        repository.delete(nullTeacher);

        Teacher t = new Teacher();
        t.setId(500L);

        Course nonExistentTeacher = DataHelper.validPersistableCourse1(d1, t);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(nonExistentTeacher));
    }

    private void assertCourseEqual(Course course1, Course course2) {
        Assertions.assertEquals(course1.getTitle(), course2.getTitle());
        Assertions.assertEquals(course1.getName(), course2.getName());
        Assertions.assertEquals(course1.getCredit(), course2.getCredit());
        Assertions.assertEquals(course1.getDescription(), course2.getDescription());
        Assertions.assertEquals(course1.getDepartment(), course2.getDepartment());
        Assertions.assertEquals(course1.getSemester(), course2.getSemester());
        Assertions.assertEquals(course1.getInstructor(), course2.getInstructor());
    }
}
