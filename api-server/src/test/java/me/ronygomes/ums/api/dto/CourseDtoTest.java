package me.ronygomes.ums.api.dto;

import me.ronygomes.ums.api.testHelper.DataHelper;
import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Semester;
import me.ronygomes.ums.api.model.Teacher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class CourseDtoTest {

    @Test
    void testToDto() {

        Department d = DataHelper.validPersistableDepartment1();
        d.setId(50L);

        Teacher t = new Teacher();
        t.setId(100L);

        Course c = DataHelper.validPersistableCourse1(d, t);
        c.setId(1L);

        CourseDto dto = CourseDto.toDto(c);
        assertCourseDto(c, dto);
    }

    @Test
    void testCopy() {
        Course to = new Course();
        CourseDto dto = new CourseDto();

        dto.setCode("1");
        dto.setName("2");
        dto.setCredit(BigDecimal.valueOf(3.0));
        dto.setDescription("4");
        dto.setDepartmentCode("5");
        dto.setSemester(Semester.FIRST_YEAR_FIRST);
        dto.setInstructorIds(List.of(500L));

        Department d = DataHelper.validPersistableDepartment1();
        d.setId(50L);

        Teacher t = new Teacher();
        t.setId(100L);

        dto.copy(to, d, List.of(t));

        Assertions.assertEquals("1", to.getCode());
        Assertions.assertEquals("2", to.getName());
        Assertions.assertEquals(3.0f, to.getCredit());
        Assertions.assertEquals("4", to.getDescription());
        Assertions.assertSame(d, to.getDepartment());
        Assertions.assertEquals(Semester.FIRST_YEAR_FIRST, to.getSemester());
        Assertions.assertTrue(to.getInstructors().contains(t));

        Assertions.assertNull(to.getId());
    }

    public static void assertCourseDto(Course c, CourseDto dto) {

        Assertions.assertEquals(c.getCode(), dto.getCode());
        Assertions.assertEquals(c.getName(), dto.getName());
        Assertions.assertEquals(c.getCredit(), dto.getCredit().floatValue());
        Assertions.assertEquals(c.getDescription(), dto.getDescription());
        Assertions.assertEquals(c.getDepartment().getCode(), dto.getDepartmentCode());
        Assertions.assertEquals(c.getSemester(), dto.getSemester());

        Assertions.assertEquals(
                c.getInstructors().stream().map(Teacher::getId).sorted().toList(),
                dto.getInstructorIds());
    }
}
