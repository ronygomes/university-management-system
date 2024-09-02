package me.ronygomes.ums.api.validator;

import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.CourseSchedule;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.repository.CourseRepository;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CourseScheduleValidatorTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CourseRepository courseRepository;

    private CourseScheduleValidator validator;

    @BeforeEach
    void setup() {
        validator = new CourseScheduleValidator(departmentRepository, courseRepository);
    }

    @Test
    void testSupports() {
        Assertions.assertTrue(validator.supports(CourseSchedule.class));
    }

    @Test
    void testNullDepartmentCode() {
        CourseSchedule sc = new CourseSchedule();
        sc.setDepartmentCode(null); // Default is null, setting for readability
        sc.setCourseId(50L);

        Course c = new Course();
        c.setId(50L);
        Mockito.when(courseRepository.findById(50L)).thenReturn(Optional.of(c));

        Errors result = new BeanPropertyBindingResult(sc, "courseSchedule");
        validator.validate(sc, result);

        Mockito.verify(departmentRepository, Mockito.never()).findByCode(Mockito.any());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getFieldError("departmentCode"));
        Assertions.assertEquals("must not be null", result.getFieldError("departmentCode").getDefaultMessage());
        Assertions.assertEquals(1, result.getErrorCount());
    }

    @Test
    void testNonExistentDepartmentCode() {
        CourseSchedule sc = new CourseSchedule();
        sc.setDepartmentCode("ABC");
        sc.setCourseId(50L);

        Course c = new Course();
        c.setId(50L);
        Mockito.when(courseRepository.findById(50L)).thenReturn(Optional.of(c));
        Mockito.when(departmentRepository.findByCode("ABC")).thenReturn(Optional.empty());

        Errors result = new BeanPropertyBindingResult(sc, "courseSchedule");
        validator.validate(sc, result);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getFieldError("departmentCode"));
        Assertions.assertEquals("department code not found", result.getFieldError("departmentCode").getDefaultMessage());
        Assertions.assertEquals(1, result.getErrorCount());
    }

    @Test
    void testNullCourseId() {
        CourseSchedule sc = new CourseSchedule();
        sc.setDepartmentCode("CSE");
        sc.setCourseId(null); // Default is null, setting for readability

        Department d = new Department();
        d.setId(50L);
        d.setCode("CSE");
        Mockito.when(departmentRepository.findByCode("CSE")).thenReturn(Optional.of(d));

        Errors result = new BeanPropertyBindingResult(sc, "courseSchedule");
        validator.validate(sc, result);

        Mockito.verify(courseRepository, Mockito.never()).findById(Mockito.any());
        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getFieldError("courseId"));
        Assertions.assertEquals("must not be null", result.getFieldError("courseId").getDefaultMessage());
        Assertions.assertEquals(1, result.getErrorCount());
    }

    @Test
    void testNonExistentCourseId() {
        CourseSchedule sc = new CourseSchedule();
        sc.setDepartmentCode("CSE");
        sc.setCourseId(99L);

        Department d = new Department();
        d.setId(50L);
        d.setCode("CSE");
        Mockito.when(departmentRepository.findByCode("CSE")).thenReturn(Optional.of(d));
        Mockito.when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        Errors result = new BeanPropertyBindingResult(sc, "courseSchedule");
        validator.validate(sc, result);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertNotNull(result.getFieldError("courseId"));
        Assertions.assertEquals("course does not exists", result.getFieldError("courseId").getDefaultMessage());
        Assertions.assertEquals(1, result.getErrorCount());
    }
}
