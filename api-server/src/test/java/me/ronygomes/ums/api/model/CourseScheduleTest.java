package me.ronygomes.ums.api.model;

import me.ronygomes.ums.api.helper.DataHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.Date;

public class CourseScheduleTest {

    private final CourseSchedule reference = createMockDBCourseSchedule();

    private CourseSchedule target;
    private CourseSchedule patch;

    @BeforeEach
    void setup() {
        target = createMockDBCourseSchedule();
        target.setDepartment(reference.getDepartment());
        target.setCourse(reference.getCourse());

        // Date test data depends on Instant.now()
        target.setStartTime(reference.getStartTime());
        target.setEndTime(reference.getEndTime());

        patch = new CourseSchedule();
    }

    @Test
    void testSemester() {
        Assertions.assertEquals(Semester.FIRST_YEAR_SECOND, target.getSemester());
        patch.setSemester(Semester.FIFTH_YEAR_FIRST);

        target.merge(patch);
        assertCourseExcept(target, "semester");
        Assertions.assertEquals(Semester.FIFTH_YEAR_FIRST, target.getSemester());
    }

    @Test
    void testBuilding() {
        Assertions.assertEquals(Building.BUILDING_1, target.getBuilding());
        patch.setBuilding(Building.BUILDING_2);

        target.merge(patch);
        assertCourseExcept(target, "building");
        Assertions.assertEquals(Building.BUILDING_2, target.getBuilding());
    }

    @Test
    void testRoomNumber() {
        Assertions.assertEquals("F7-102", target.getRoomNumber());
        patch.setRoomNumber("Test");

        target.merge(patch);
        assertCourseExcept(target, "roomNumber");
        Assertions.assertEquals("Test", target.getRoomNumber());
    }

    @Test
    void testDay() {
        Assertions.assertEquals(DayOfWeek.MONDAY, target.getDay());
        patch.setDay(DayOfWeek.FRIDAY);

        target.merge(patch);
        assertCourseExcept(target, "day");
        Assertions.assertEquals(DayOfWeek.FRIDAY, target.getDay());
    }

    @Test
    void testStartTime() {
        Assertions.assertSame(reference.getStartTime(), target.getStartTime());

        Date date = new Date();
        patch.setStartTime(date);

        target.merge(patch);
        assertCourseExcept(target, "startTime");
        Assertions.assertSame(date, target.getStartTime());
    }

    @Test
    void testEndTime() {
        Assertions.assertSame(reference.getEndTime(), target.getEndTime());

        Date date = new Date();
        patch.setEndTime(date);

        target.merge(patch);
        assertCourseExcept(target, "endTime");
        Assertions.assertSame(date, target.getEndTime());
    }

    @Test
    void testCourseId() {
        // Current getter implementation returns Course#id if courseId is null
        Assertions.assertEquals(2, target.getCourseId());
        patch.setCourseId(50L);

        target.merge(patch);
        assertCourseExcept(target, "courseId");
        Assertions.assertSame(50L, target.getCourseId());
    }

    @Test
    void testCourseIdReadsFromCourse() {
        Assertions.assertEquals(2, target.getCourseId());
        target.setCourse(null);
        Assertions.assertNull(target.getCourseId());
    }

    @Test
    void testDepartmentCode() {
        Assertions.assertNull(target.getDepartmentCode());
        patch.setDepartmentCode("123");

        target.merge(patch);
        assertCourseExcept(target, "departmentCode");
        Assertions.assertEquals("123", target.getDepartmentCode());
    }

    private void assertCourseExcept(CourseSchedule merged, String field) {
        // Unmodified fields
        Assertions.assertEquals(reference.getId(), merged.getId());
        Assertions.assertSame(reference.getCourse(), merged.getCourse());
        Assertions.assertSame(reference.getDepartment(), merged.getDepartment());

        if (!"semester".equals(field)) {
            Assertions.assertEquals(reference.getSemester(), merged.getSemester());
        }

        if (!"building".equals(field)) {
            Assertions.assertEquals(reference.getBuilding(), merged.getBuilding());
        }

        if (!"roomNumber".equals(field)) {
            Assertions.assertEquals(reference.getRoomNumber(), merged.getRoomNumber());
        }

        if (!"day".equals(field)) {
            Assertions.assertEquals(reference.getDay(), merged.getDay());
        }

        if (!"startTime".equals(field)) {
            Assertions.assertEquals(reference.getStartTime(), merged.getStartTime());
        }

        if (!"endTime".equals(field)) {
            Assertions.assertEquals(reference.getEndTime(), merged.getEndTime());
        }

        if (!"courseId".equals(field)) {
            Assertions.assertEquals(reference.getCourseId(), merged.getCourseId());
        }

        if (!"departmentCode".equals(field)) {
            Assertions.assertEquals(reference.getDepartmentCode(), merged.getDepartmentCode());
        }
    }

    private CourseSchedule createMockDBCourseSchedule() {
        Department d = DataHelper.validPersistableDepartment1();
        d.setId(1L);

        Course c = DataHelper.validPersistableCourse1(d, null);
        c.setId(2L);

        CourseSchedule dbCs = DataHelper.validPersistableCourseSchedule1(d, c);
        dbCs.setId(3L);

        return dbCs;
    }
}
