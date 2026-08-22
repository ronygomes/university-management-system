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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionSystemException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.ronygomes.ums.api.testHelper.TestHelper.*;

@SpringBootTest
@ActiveProfiles("database-test")
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
        cs2.setEnrollmentOpen(false);

        repository.save(cs2);

        CourseSchedule dbCourseSchedule = repository.findById(cs.getId()).orElseThrow();
        assertCourseScheduleEqual(cs2, dbCourseSchedule);

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
    void testFieldConstrain_slots() {
        for (DayOfWeek d : DayOfWeek.values()) {
            Assertions.assertTrue(d.name().length() <= 20);
        }

        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);
        cs.setSlots(null);

        Throwable exNull = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(cs));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exNull);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be empty", v.getMessage());
            Assertions.assertEquals("slots", v.getPropertyPath().toString());
        });

        CourseSchedule csEmpty = DataHelper.validPersistableCourseSchedule1(department, course);
        csEmpty.setSlots(Collections.emptyList());
        Throwable exEmpty = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(csEmpty));
        Set<ConstraintViolation<?>> emptyViolations = extractConstraintViolation(exEmpty);
        Assertions.assertEquals(1, emptyViolations.size());
        emptyViolations.forEach(v -> {
            Assertions.assertEquals("must not be empty", v.getMessage());
            Assertions.assertEquals("slots", v.getPropertyPath().toString());
        });
    }

    @Test
    void testFieldConstrain_startTime() {
        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);
        cs.setStartDate(null);
        repository.save(cs);

        Assertions.assertNull(repository.findById(cs.getId()).orElseThrow().getStartDate());
        repository.delete(cs);
    }

    @Test
    void testFieldConstrain_endTime() {
        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(department, course);
        cs.setEndDate(null);
        repository.save(cs);

        Assertions.assertNull(repository.findById(cs.getId()).orElseThrow().getEndDate());
        repository.delete(cs);
    }

    @Test
    void testFindByCourseId() {
        Department department = departmentRepository.findByCode("EEE").orElseThrow();
        CourseSchedule cs1 = DataHelper.validPersistableCourseSchedule1(department, course);
        CourseSchedule cs2 = DataHelper.validPersistableCourseSchedule2(department, course);

        Course course2 = DataHelper.validPersistableCourse2(department, null);
        courseRepository.save(course2);

        repository.save(cs1);
        repository.save(cs2);
        CourseSchedule cs3 = DataHelper.validPersistableCourseSchedule1(department, course2);
        repository.save(cs3);

        List<CourseSchedule> schedules = repository.findByCourseId(course.getId());
        Assertions.assertEquals(2, schedules.size());

        CourseSchedule dbCs1 = schedules.stream().filter(cs -> cs.getRoomNumber()
                .equals("F7-102")).findFirst().orElseThrow();
        assertCourseScheduleEqual(cs1, dbCs1);
        Assertions.assertNotNull(dbCs1.getId());

        CourseSchedule dbCs2 = schedules.stream().filter(cs -> cs.getRoomNumber()
                .equals("F7-202")).findFirst().orElseThrow();
        assertCourseScheduleEqual(cs2, dbCs2);
        Assertions.assertNotNull(dbCs2.getId());

        repository.delete(cs1);
        repository.delete(cs2);
        repository.delete(cs3);
        courseRepository.delete(course2);
    }

    private void assertCourseScheduleEqual(CourseSchedule cs1, CourseSchedule cs2) {
        Assertions.assertEquals(cs1.getDepartment(), cs2.getDepartment());
        Assertions.assertEquals(cs1.getSemester(), cs2.getSemester());
        Assertions.assertEquals(cs1.getCourse(), cs2.getCourse());
        Assertions.assertEquals(cs1.getBuilding(), cs2.getBuilding());
        Assertions.assertEquals(cs1.getRoomNumber(), cs2.getRoomNumber());
        Assertions.assertIterableEquals(cs1.getSlots(), cs2.getSlots());
        Assertions.assertEquals(0, cs1.getStartDate().compareTo(cs2.getStartDate()));
        Assertions.assertEquals(0, cs1.getEndDate().compareTo(cs2.getEndDate()));
        Assertions.assertEquals(cs1.isEnrollmentOpen(), cs2.isEnrollmentOpen());
    }
}
