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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionSystemException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.util.Set;

import static me.ronygomes.ums.api.helper.TestHelper.extractConstraintViolation;
import static me.ronygomes.ums.api.helper.TestHelper.isEnumFieldStoredAsString;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers(disabledWithoutDocker = true)
public class CourseScheduleRepositoryTest {

    @Autowired
    private CourseScheduleRepository repository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Course course;

    @BeforeEach
    void setup() {
        Department d = departmentRepository.findByCode("CSE").orElseThrow();
        course = DataHelper.validPersistableCourse1(d, null);
        courseRepository.save(course);
    }

    @AfterEach
    void tearDown() {
        Assertions.assertEquals(0, repository.findAll().size());
        courseRepository.delete(course);
    }

    @Test
    void testSave() {
        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);

        repository.save(cs);
        assertCourseScheduleEqual(cs, repository.findById(cs.getId()).orElseThrow());

        repository.delete(cs);
    }

    @Test
    void testUpdate() {
        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);

        repository.save(cs);
        Department department2 = departmentRepository.findByCode("CE").orElseThrow();
        Course course2 = DataHelper.validPersistableCourse2(department2, null);
        courseRepository.save(course2);

        CourseSchedule cs2 = DataHelper.validPersistableCourseSchedule2(department2, course2);
        cs2.setUuid(cs.getUuid());
        cs2.setId(cs.getId());
        cs2.setVersion(cs.getVersion());

        repository.save(cs2);

        CourseSchedule dbCourseSchedule =  repository.findById(cs.getId()).orElseThrow();
        assertCourseScheduleEqual(cs2,dbCourseSchedule);

        repository.delete(dbCourseSchedule);
        courseRepository.delete(course2);
    }

    @Test
    void testFieldConstrain_department() {
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(null, course);
        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(cs));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("department", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });
    }

    @Test
    void testFieldConstrain_semester() {
        Assertions.assertTrue(isEnumFieldStoredAsString(CourseSchedule.class, "semester"));

        for (Semester s : Semester.values()) {
            Assertions.assertTrue(s.name().length() <= 30);
        }

        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);
        cs.setSemester(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(cs));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("semester", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });
    }

    @Test
    void testFieldConstrain_course() {
        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, null);
        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(cs));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("course", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });
    }

    @Test
    void testFieldConstrain_building() {
        Assertions.assertTrue(isEnumFieldStoredAsString(CourseSchedule.class, "building"));

        for (Building b : Building.values()) {
            Assertions.assertTrue(b.name().length() <= 30);
        }

        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);
        cs.setBuilding(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(cs));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("building", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });
    }

    @Test
    void testFieldConstrain_roomNumber() {

        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);
        cs.setRoomNumber(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(cs));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("roomNumber", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        CourseSchedule csMin = DataHelper.validPersistableCourseSchedule1(department, course);
        csMin.setRoomNumber("");

        Throwable exMin = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(csMin));
        Set<ConstraintViolation<?>> minViolations = extractConstraintViolation(exMin);
        Assertions.assertEquals(1, minViolations.size());
        minViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 100", v.getMessage());
            Assertions.assertEquals("roomNumber", v.getPropertyPath().toString());
            Assertions.assertEquals("", v.getInvalidValue());
        });

        CourseSchedule csMax = DataHelper.validPersistableCourseSchedule1(department, course);
        csMax.setRoomNumber("a".repeat(101));

        Throwable exMax = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(csMax));
        Set<ConstraintViolation<?>> maxViolations = extractConstraintViolation(exMax);
        Assertions.assertEquals(1, maxViolations.size());
        maxViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 100", v.getMessage());
            Assertions.assertEquals("roomNumber", v.getPropertyPath().toString());
            Assertions.assertEquals("a".repeat(101), v.getInvalidValue());
        });
    }

    @Test
    void testFieldConstrain_day() {
        Assertions.assertTrue(isEnumFieldStoredAsString(CourseSchedule.class, "day"));

        for (DayOfWeek d : DayOfWeek.values()) {
            Assertions.assertTrue(d.name().length() <= 20);
        }

        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);
        cs.setDay(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(cs));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("day", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });
    }

    @Test
    void testFieldConstrain_startTime() {
        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);
        cs.setStartTime(null);
        repository.save(cs);

        Assertions.assertNull(repository.findById(cs.getId()).orElseThrow().getStartTime());
        repository.delete(cs);
    }

    @Test
    void testFieldConstrain_endTime() {
        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);
        cs.setEndTime(null);
        repository.save(cs);

        Assertions.assertNull(repository.findById(cs.getId()).orElseThrow().getEndTime());
        repository.delete(cs);
    }

    private void assertCourseScheduleEqual(CourseSchedule cs1, CourseSchedule cs2) {
        Assertions.assertEquals(cs1.getDepartment(), cs2.getDepartment());
        Assertions.assertEquals(cs1.getSemester(), cs2.getSemester());
        Assertions.assertEquals(cs1.getCourse(), cs2.getCourse());
        Assertions.assertEquals(cs1.getBuilding(), cs2.getBuilding());
        Assertions.assertEquals(cs1.getRoomNumber(), cs2.getRoomNumber());
        Assertions.assertEquals(cs1.getDay(), cs2.getDay());
        Assertions.assertEquals(0, cs1.getStartTime().compareTo(cs2.getStartTime()));
        Assertions.assertEquals(0, cs1.getEndTime().compareTo(cs2.getEndTime()));
    }
}
