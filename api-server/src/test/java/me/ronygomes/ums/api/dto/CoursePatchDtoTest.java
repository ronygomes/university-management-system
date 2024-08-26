package me.ronygomes.ums.api.dto;

import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Semester;
import me.ronygomes.ums.api.model.Teacher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class CoursePatchDtoTest {

    private Course mockDBCourse;
    private CoursePatchDto dto;

    @BeforeEach
    void setup() {
        mockDBCourse = createMockCourse();
        dto = new CoursePatchDto();
    }

    @Test
    void testCoursePatchDto_title() {
        dto.setTitle("Updated");
        CourseDto idto = dto.toInputDto(mockDBCourse);
        assertEqualsExcept(mockDBCourse, idto, "title");
        Assertions.assertEquals("Updated", idto.getTitle());
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
    void testCoursePatchDto_instructorId() {
        dto.setInstructorId(1000L);
        CourseDto idto = dto.toInputDto(mockDBCourse);
        assertEqualsExcept(mockDBCourse, idto, "instructorId");
        Assertions.assertEquals(1000L, idto.getInstructorId());
    }

    private void assertEqualsExcept(Course mockDBCourse, CourseDto idto, String field) {
        if (!"title".equals(field)) {
            Assertions.assertEquals(mockDBCourse.getTitle(), idto.getTitle());
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

        if (!"instructorId".equals(field)) {
            Assertions.assertEquals(mockDBCourse.getInstructor().getId(), idto.getInstructorId());
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
