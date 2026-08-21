package me.ronygomes.ums.api.service;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.config.TestContextConfig;
import me.ronygomes.ums.api.dto.CourseDto;
import me.ronygomes.ums.api.dto.CoursePatchDto;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.testHelper.DataHelper;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Semester;
import me.ronygomes.ums.api.model.Teacher;
import me.ronygomes.ums.api.repository.CourseRepository;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import me.ronygomes.ums.api.repository.TeacherRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static me.ronygomes.ums.api.dto.CourseDtoTest.assertCourseDto;

@SpringBootTest
@ContextConfiguration(classes = {TestContextConfig.class})
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ExceptionHelper exceptionHelper;

    @Mock
    private Validator mockValidator;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Autowired
    Validator validator;

    private CourseService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        service = new CourseService(courseRepository, departmentRepository,
                teacherRepository, exceptionHelper, mockValidator);
    }

    @Test
    void testFindAll() {
        Department d = DataHelper.validPersistableDepartment1();
        Teacher t = new Teacher();
        t.setId(7L);

        Course c1 = DataHelper.validPersistableCourse1(d, t);
        c1.setId(1L);
        Course c2 = DataHelper.validPersistableCourse1(d, t);
        c2.setId(2L);

        Mockito.when(courseRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CourseDto> result = service.findAll();

        Assertions.assertEquals(2, result.size());
        assertCourseDto(c1, result.get(0));
        assertCourseDto(c2, result.get(1));
        Assertions.assertEquals(1L, result.get(0).getId());
        Assertions.assertEquals(2L, result.get(1).getId());
    }

    @Test
    void testFindByIdSuccess() {
        Course c = DataHelper.validPersistableCourse1(new Department(), new Teacher());
        c.setId(1L);

        Mockito.when(courseRepository.findById(1L)).thenReturn(Optional.of(c));
        CourseDto dto = service.findById(1L);

        assertCourseDto(c, dto);
    }

    @Test
    void testFindByIdFailure() {
        Mockito.when(courseRepository.findById(500L)).thenReturn(Optional.empty());

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> service.findById(500L));
        Assertions.assertEquals(ExceptionType.ENTITY_NOT_FOUND, ex.getExceptionType());
        Assertions.assertEquals("Course with id '500' not found", ex.getErrorDetails());
        Assertions.assertEquals(0, ex.getErrors().size());
    }

    @Test
    void testCreateSuccess() {
        CourseDto dto = validCourseDto();

        Mockito.when(mockValidator.validate(dto)).thenReturn(new HashSet<>());

        Department d = DataHelper.validPersistableDepartment1();
        Mockito.when(departmentRepository.findByCode(d.getCode())).thenReturn(Optional.of(d));

        Teacher teacher = new Teacher();
        teacher.setId(500L);

        Mockito.when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

        ArgumentCaptor<Course> ac = ArgumentCaptor.forClass(Course.class);
        Mockito.doAnswer(i -> {
            Course t = (Course) i.getArguments()[0];
            t.setId(100L);
            return null;
        }).when(courseRepository).save(ac.capture());

        long id = service.create(dto);

        Course c = ac.getValue();
        assertCourseDto(c, dto);
        Assertions.assertEquals(100L, id);

        Mockito.verify(courseRepository, Mockito.times(1)).flush();

        dto.setInstructorId(null);
        service.create(dto);
        Assertions.assertEquals(100L, id);
        assertCourseDto(c, dto);
        Assertions.assertNull(ac.getValue().getInstructor());
    }

    @Test
    void testCreateFailureValidationErrorInDtoData() {
        service = new CourseService(courseRepository, departmentRepository,
                teacherRepository, exceptionHelper, validator);

        CourseDto dto = validCourseDto();
        dto.setCode(null);

        Department d = DataHelper.validPersistableDepartment1();
        Mockito.when(departmentRepository.findByCode(d.getCode())).thenReturn(Optional.of(d));

        Teacher teacher = new Teacher();
        teacher.setId(500L);

        Mockito.when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> service.create(dto));
        Assertions.assertEquals(ExceptionType.DATA_VALIDATION_FAILED, ex.getExceptionType());
        Assertions.assertEquals("Not a valid Course. See 'error' field for details", ex.getErrorDetails());
        Assertions.assertEquals(1, ex.getErrors().size());
        Assertions.assertEquals("code", ex.getErrors().get(0).getField());

        Mockito.verify(courseRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(courseRepository, Mockito.never()).flush();
    }

    @Test
    void testCreateFailureValidationErrorInDepartment() {
        service = new CourseService(courseRepository, departmentRepository,
                teacherRepository, exceptionHelper, validator);

        CourseDto dto = validCourseDto();

        Mockito.when(departmentRepository.findByCode(dto.getDepartmentCode())).thenReturn(Optional.empty());

        Teacher teacher = new Teacher();
        teacher.setId(500L);

        Mockito.when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> service.create(dto));
        Assertions.assertEquals(ExceptionType.DATA_VALIDATION_FAILED, ex.getExceptionType());
        Assertions.assertEquals("Not a valid Course. See 'error' field for details", ex.getErrorDetails());
        Assertions.assertEquals(1, ex.getErrors().size());
        Assertions.assertEquals("departmentCode", ex.getErrors().get(0).getField());
        Assertions.assertEquals("Department code not found", ex.getErrors().get(0).getMessage());

        Mockito.verify(courseRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(courseRepository, Mockito.never()).flush();
    }

    @Test
    void testCreateFailureValidationErrorInTeacher() {
        service = new CourseService(courseRepository, departmentRepository,
                teacherRepository, exceptionHelper, validator);

        CourseDto dto = validCourseDto();

        Department d = DataHelper.validPersistableDepartment1();
        Mockito.when(departmentRepository.findByCode(d.getCode())).thenReturn(Optional.of(d));

        Mockito.when(teacherRepository.findById(500L)).thenReturn(Optional.empty());

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> service.create(dto));
        Assertions.assertEquals(ExceptionType.DATA_VALIDATION_FAILED, ex.getExceptionType());
        Assertions.assertEquals("Not a valid Course. See 'error' field for details", ex.getErrorDetails());
        Assertions.assertEquals(1, ex.getErrors().size());
        Assertions.assertEquals("instructorId", ex.getErrors().get(0).getField());
        Assertions.assertEquals("Teacher not found", ex.getErrors().get(0).getMessage());

        Mockito.verify(courseRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(courseRepository, Mockito.never()).flush();
    }

    @Test
    void testUpdateAllSuccess() {
        CourseDto dto = validCourseDto();

        Mockito.when(mockValidator.validate(dto)).thenReturn(new HashSet<>());

        Department d = DataHelper.validPersistableDepartment1();
        Mockito.when(departmentRepository.findByCode(d.getCode())).thenReturn(Optional.of(d));

        Teacher teacher = new Teacher();
        teacher.setId(500L);

        Mockito.when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

        Course dbCourse = DataHelper.validPersistableCourse1(d, teacher);
        dbCourse.setId(101L);
        Mockito.when(courseRepository.findById(101L)).thenReturn(Optional.of(dbCourse));

        ArgumentCaptor<Course> ac = ArgumentCaptor.forClass(Course.class);
        Mockito.when(courseRepository.save(ac.capture())).thenReturn(null);

        service.updateAll(101L, dto);

        Course c = ac.getValue();
        assertCourseDto(c, dto);

        Mockito.verify(courseRepository, Mockito.times(1)).flush();
    }

    @Test
    void updateProvidedSuccess() {
        CoursePatchDto patchDto = new CoursePatchDto();
        patchDto.setCode("Updated");
        patchDto.setDepartmentCode("QQQ");
        patchDto.setInstructorId(1000L);

        Department d = DataHelper.validPersistableDepartment1();
        Mockito.when(departmentRepository.findByCode(d.getCode())).thenReturn(Optional.of(d));

        Teacher teacher = new Teacher();
        teacher.setId(500L);

        Mockito.when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

        Course dbCourse = DataHelper.validPersistableCourse1(d, teacher);
        dbCourse.setId(101L);

        Mockito.when(courseRepository.findById(101L)).thenReturn(Optional.of(dbCourse));

        Department dept = new Department();
        dept.setId(11L);
        dept.setCode("QQQ");
        dept.setName("Quality Quick Quite");

        Mockito.when(departmentRepository.findByCode("QQQ")).thenReturn(Optional.of(dept));

        Teacher updateTeacher = new Teacher();
        updateTeacher.setId(1000L);
        Mockito.when(teacherRepository.findById(1000L)).thenReturn(Optional.of(updateTeacher));

        ArgumentCaptor<Course> ac = ArgumentCaptor.forClass(Course.class);
        Mockito.when(courseRepository.save(ac.capture())).thenReturn(null);

        service.updateProvided(101L, patchDto);

        Course arg = ac.getValue();
        Assertions.assertEquals(patchDto.getCode(), arg.getCode());
        Assertions.assertEquals(patchDto.getDepartmentCode(), arg.getDepartment().getCode());
        Assertions.assertEquals(patchDto.getInstructorId(), arg.getInstructor().getId());

        Assertions.assertNull(patchDto.getName());
        Assertions.assertEquals(dbCourse.getName(), arg.getName());

        Assertions.assertNull(patchDto.getCredit());
        Assertions.assertEquals(dbCourse.getCredit(), arg.getCredit());

        Assertions.assertNull(patchDto.getDescription());
        Assertions.assertEquals(dbCourse.getDescription(), arg.getDescription());

        Assertions.assertNull(patchDto.getSemester());
        Assertions.assertEquals(dbCourse.getSemester(), arg.getSemester());

        Mockito.verify(courseRepository, Mockito.times(1)).flush();
    }

    @Test
    void testDelete() {
        Course c = DataHelper.validPersistableCourse1(new Department(), new Teacher());
        c.setId(1L);
        Mockito.when(courseRepository.findById(1L)).thenReturn(Optional.of(c));

        service.delete(1L);

        Mockito.verify(courseRepository, Mockito.times(1)).delete(c);
    }

    private CourseDto validCourseDto() {
        CourseDto dto = new CourseDto();
        dto.setName("Course-1");
        dto.setCode("Course-Title-1");
        dto.setSemester(Semester.SECOND_YEAR_FIRST);
        dto.setDescription("Description");
        dto.setCredit(BigDecimal.valueOf(3.5f));
        dto.setDepartmentCode("CODE-1");
        dto.setInstructorId(500L);

        return dto;
    }
}
