package me.ronygomes.ums.api.dto;

import me.ronygomes.ums.api.testHelper.DataHelper;
import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Semester;
import me.ronygomes.ums.api.model.Teacher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class CoursePatchDtoTest {

    private Course mockDBCourse;
    private CoursePatchDto dto;

    @BeforeEach
    void setup() {
        mockDBCourse = createMockCourse();
        dto = new CoursePatchDto();
    }

    @Test
    void testCoursePatchDto_code() {
        dto.setCode("Updated");
        CourseDto idto = dto.toInputDto(mockDBCourse);
        assertEqualsExcept(mockDBCourse, idto, "code");
        Assertions.assertEquals("Updated", idto.getCode());
    }

    @Test
    void testCoursePatchDto_name() {
        dto.setName("Updated");
        CourseDto idto = dto.toInputDto(mockDBCourse);
        assertEqualsExcept(mockDBCourse, idto, "name");
        Assertions.assertEquals("Updated", idto.getName());
    }

    @Test
    void testCoursePatchDto_credit() {
        dto.setCredit(BigDecimal.valueOf(100f));
        CourseDto idto = dto.toInputDto(mockDBCourse);
        assertEqualsExcept(mockDBCourse, idto, "credit");
        Assertions.assertEquals(100f, idto.getCredit().floatValue());
    }

    @Test
    void testCoursePatchDto_description() {
        dto.setDescription("Updated");
        CourseDto idto = dto.toInputDto(mockDBCourse);
        assertEqualsExcept(mockDBCourse, idto, "description");
        Assertions.assertEquals("Updated", idto.getDescription());
    }

    @Test
    void testCoursePatchDto_departmentCode() {
        dto.setDepartmentCode("Updated");
        CourseDto idto = dto.toInputDto(mockDBCourse);
        assertEqualsExcept(mockDBCourse, idto, "departmentCode");
        Assertions.assertEquals("Updated", idto.getDepartmentCode());
    }

    @Test
    void testCoursePatchDto_semester() {
        dto.setSemester(Semester.FOURTH_YEAR_FIRST);
        CourseDto idto = dto.toInputDto(mockDBCourse);
        assertEqualsExcept(mockDBCourse, idto, "semester");
        Assertions.assertEquals(Semester.FOURTH_YEAR_FIRST, idto.getSemester());
    }

    @Test
    void testCoursePatchDto_instructorIds() {
        dto.setInstructorIds(List.of(1000L));
        CourseDto idto = dto.toInputDto(mockDBCourse);
        assertEqualsExcept(mockDBCourse, idto, "instructorIds");
        Assertions.assertEquals(List.of(1000L), idto.getInstructorIds());
    }

    private void assertEqualsExcept(Course mockDBCourse, CourseDto idto, String field) {
        if (!"code".equals(field)) {
            Assertions.assertEquals(mockDBCourse.getCode(), idto.getCode());
        }

        if (!"name".equals(field)) {
            Assertions.assertEquals(mockDBCourse.getName(), idto.getName());
        }

        if (!"credit".equals(field)) {
            Assertions.assertEquals(mockDBCourse.getCredit(), idto.getCredit().floatValue());
        }

        if (!"description".equals(field)) {
            Assertions.assertEquals(mockDBCourse.getDescription(), idto.getDescription());
        }

        if (!"departmentCode".equals(field)) {
            Assertions.assertEquals(mockDBCourse.getDepartment().getCode(), idto.getDepartmentCode());
        }

        if (!"semester".equals(field)) {
            Assertions.assertEquals(mockDBCourse.getSemester(), idto.getSemester());
        }

        if (!"instructorIds".equals(field)) {
            Assertions.assertEquals(
                    mockDBCourse.getInstructors().stream().map(Teacher::getId).sorted().toList(),
                    idto.getInstructorIds());
        }
    }

    private Course createMockCourse() {
        Department d = DataHelper.validPersistableDepartment1();
        d.setId(200L);

        Teacher t = new Teacher();
        t.setId(300L);

        Course c = DataHelper.validPersistableCourse1(d, t);
        c.setId(100L);

        return c;
    }
}
