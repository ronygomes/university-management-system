package me.ronygomes.ums.api.dto;

import me.ronygomes.ums.api.testHelper.DataHelper;
import me.ronygomes.ums.api.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class EnrollmentDtoTest {

    private Enrollment enrollment;
    private EnrollmentDto dto;

    @BeforeEach
    void setup() {
        Department d = new Department();
        Student s = DataHelper.validPersistableStudent1(d);
        s.setId(2L);
        Course c = DataHelper.validPersistableCourse1(d, null);
        c.setId(3L);

        enrollment = DataHelper.validPersistableEnrollment1(s, d, c);
        enrollment.setId(1L);

        CourseSchedule sc = enrollment.getCourseSchedule();
        sc.setId(4L);

        dto = new EnrollmentDto();
    }

    @Test
    void testToEntity() {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setId(1L);
        dto.setCourseScheduleId(2L);
        dto.setGrade(Grade.A);
        dto.setStatus(EnrollmentStatus.PASSED);

        Date now = new Date();
        dto.setEnrollmentDate(now);

        Enrollment e = dto.toEnrollment(null);

        Assertions.assertNull(e.getId());
        Assertions.assertNull(e.getCourseSchedule());
        Assertions.assertNull(e.getStudent());
        Assertions.assertEquals(Grade.A, e.getGrade());
        Assertions.assertEquals(EnrollmentStatus.PASSED, e.getStatus());
        Assertions.assertSame(now, e.getEnrollmentDate());

        e = dto.toEnrollment(2L);
        Assertions.assertEquals(2L, e.getId());
    }

    @Test
    void testStudentId() {
        Assertions.assertEquals(2L, enrollment.getStudent().getId());
        dto.setStudentId(200L);
        dto.mergeWith(enrollment);

        assertDataEquals(dto, "studentId");
        Assertions.assertEquals(200L, dto.getStudentId());
    }

    @Test
    void testCourseId() {
        Assertions.assertEquals(4L, enrollment.getCourseSchedule().getId());
        dto.setCourseScheduleId(300L);
        dto.mergeWith(enrollment);

        assertDataEquals(dto, "courseScheduleId");
        Assertions.assertEquals(300L, dto.getCourseScheduleId());
    }

    @Test
    void testEnrollmentDate() {
        Date oldEnrollmentDate = enrollment.getEnrollmentDate();
        Assertions.assertNotNull(oldEnrollmentDate);

        // Added 100L as sometime fails assertNotEquals() as becomes equals till milliseconds precisions
        Date newEnrollmentDate = new Date(new Date().getTime() + 100L);
        dto.setEnrollmentDate(newEnrollmentDate);
        dto.mergeWith(enrollment);

        assertDataEquals(dto, "enrollmentDate");
        Assertions.assertSame(newEnrollmentDate, dto.getEnrollmentDate());
        Assertions.assertNotEquals(newEnrollmentDate, oldEnrollmentDate);
    }

    @Test
    void testStatus() {
        Assertions.assertEquals(EnrollmentStatus.ON_GOING, enrollment.getStatus());
        dto.setStatus(EnrollmentStatus.PASSED);
        dto.mergeWith(enrollment);

        assertDataEquals(dto, "status");
        Assertions.assertEquals(EnrollmentStatus.PASSED, dto.getStatus());
    }

    @Test
    void testGrade() {
        Assertions.assertEquals(Grade.A, enrollment.getGrade());
        dto.setGrade(Grade.F);
        dto.mergeWith(enrollment);

        assertDataEquals(dto, "grade");
        Assertions.assertEquals(Grade.F, dto.getGrade());
    }

    private void assertDataEquals(EnrollmentDto dto, String field) {
        if (!"studentId".equals(field)) {
            Assertions.assertEquals(enrollment.getStudent().getId(), dto.getStudentId());
        }

        if (!"courseScheduleId".equals(field)) {
            Assertions.assertEquals(enrollment.getCourseSchedule().getId(), dto.getCourseScheduleId());
        }

        if (!"enrollmentDate".equals(field)) {
            Assertions.assertEquals(enrollment.getEnrollmentDate(), dto.getEnrollmentDate());
        }

        if (!"status".equals(field)) {
            Assertions.assertEquals(enrollment.getStatus(), dto.getStatus());
        }

        if (!"grade".equals(field)) {
            Assertions.assertEquals(enrollment.getGrade(), dto.getGrade());
        }
    }
}
