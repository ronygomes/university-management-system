package me.ronygomes.ums.api.service;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.config.TestContextConfig;
import me.ronygomes.ums.api.dto.TeacherDto;
import me.ronygomes.ums.api.dto.TeacherPatchInputDto;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.testHelper.DataHelper;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Designation;
import me.ronygomes.ums.api.model.Teacher;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import me.ronygomes.ums.api.repository.DesignationRepository;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static me.ronygomes.ums.api.testHelper.DataHelper.validTeacherInputDto;

@SpringBootTest
@ContextConfiguration(classes = {TestContextConfig.class})
public class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DesignationRepository designationRepository;

    @Mock
    private Validator mockValidator;

    @Mock
    private ExceptionHelper exceptionHelper;

    @Autowired
    private Validator validator;

    private TeacherService teacherService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        teacherService = new TeacherService(teacherRepository, departmentRepository,
                designationRepository, mockValidator, exceptionHelper);
    }

    @Test
    void findByAll() {
        List<Teacher> teachers = new ArrayList<>();

        Mockito.when(teacherRepository.findAll()).thenReturn(teachers);
        Assertions.assertSame(teachers, teacherService.findAll());
    }

    @Test
    void findById_success() {
        Teacher t = new Teacher();
        Mockito.when(teacherRepository.findById(1L)).thenReturn(Optional.of(t));
        Assertions.assertSame(t, teacherService.findById(1L));
    }

    @Test
    void findById_error() {
        Mockito.when(teacherRepository.findById(1L)).thenReturn(Optional.empty());
        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> teacherService.findById(1L));

        Assertions.assertEquals(ExceptionType.ENTITY_NOT_FOUND, ex.getExceptionType());
        Assertions.assertEquals("Teacher with id=1 not found", ex.getErrorDetails());
        Assertions.assertEquals(0, ex.getErrors().size());
    }

    @Test
    void create_success() {
        TeacherDto dto = validTeacherInputDto();

        Mockito.when(mockValidator.validate(dto)).thenReturn(new HashSet<>());

        Department d = new Department();
        Mockito.when(departmentRepository.findByCode(dto.getDepartmentCode())).thenReturn(Optional.of(d));

        Designation title = new Designation();
        Mockito.when(designationRepository.findByTitle(dto.getTitle())).thenReturn(Optional.of(title));

        ArgumentCaptor<Teacher> ac = ArgumentCaptor.forClass(Teacher.class);
        Mockito.doAnswer(i -> {
            Teacher t = (Teacher) i.getArguments()[0];
            t.setId(1L);
            return null;
        }).when(teacherRepository).save(ac.capture());

        teacherService.create(dto);

        Teacher t = ac.getValue();
        assertTeacherDataEquals(t, dto, d, title);

        Mockito.verify(teacherRepository, Mockito.times(1)).flush();
    }

    @Test
    void create_validationErrorInDtoData() {
        teacherService = new TeacherService(teacherRepository, departmentRepository,
                designationRepository, validator, exceptionHelper);

        TeacherDto dto = validTeacherInputDto();
        dto.setFullName(null);

        Department d = new Department();
        Mockito.when(departmentRepository.findByCode(dto.getDepartmentCode())).thenReturn(Optional.of(d));

        Designation title = new Designation();
        Mockito.when(designationRepository.findByTitle(dto.getTitle())).thenReturn(Optional.of(title));

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> teacherService.create(dto));
        Assertions.assertEquals(ExceptionType.DATA_VALIDATION_FAILED, ex.getExceptionType());
        Assertions.assertEquals("Not a valid Teacher. See 'error' field for details", ex.getErrorDetails());
        Assertions.assertEquals(1, ex.getErrors().size());
        Assertions.assertEquals("fullName", ex.getErrors().get(0).getField());

        Mockito.verify(teacherRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(teacherRepository, Mockito.never()).flush();
    }

    @Test
    void create_validationErrorForDepartment() {

        TeacherDto dto = validTeacherInputDto();
        Mockito.when(mockValidator.validate(dto)).thenReturn(new HashSet<>());
        Mockito.when(departmentRepository.findByCode(dto.getDepartmentCode())).thenReturn(Optional.empty());

        Designation title = new Designation();
        Mockito.when(designationRepository.findByTitle(dto.getTitle())).thenReturn(Optional.of(title));

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> teacherService.create(dto));
        Assertions.assertEquals(ExceptionType.DATA_VALIDATION_FAILED, ex.getExceptionType());
        Assertions.assertEquals("Not a valid Teacher. See 'error' field for details", ex.getErrorDetails());
        Assertions.assertEquals(1, ex.getErrors().size());
        Assertions.assertEquals("departmentCode", ex.getErrors().get(0).getField());
        Assertions.assertEquals("Department code not found", ex.getErrors().get(0).getMessage());

        Mockito.verify(teacherRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(teacherRepository, Mockito.never()).flush();
    }

    @Test
    void create_validationErrorForTitle() {

        TeacherDto dto = validTeacherInputDto();
        Mockito.when(mockValidator.validate(dto)).thenReturn(new HashSet<>());

        Department d = new Department();
        Mockito.when(departmentRepository.findByCode(dto.getDepartmentCode())).thenReturn(Optional.of(d));
        Mockito.when(designationRepository.findByTitle(dto.getTitle())).thenReturn(Optional.empty());

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> teacherService.create(dto));
        Assertions.assertEquals(ExceptionType.DATA_VALIDATION_FAILED, ex.getExceptionType());
        Assertions.assertEquals("Not a valid Teacher. See 'error' field for details", ex.getErrorDetails());
        Assertions.assertEquals(1, ex.getErrors().size());
        Assertions.assertEquals("title", ex.getErrors().get(0).getField());
        Assertions.assertEquals("Title not found", ex.getErrors().get(0).getMessage());

        Mockito.verify(teacherRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(teacherRepository, Mockito.never()).flush();
    }

    @Test
    void updateAll_success() {
        TeacherDto dto = validTeacherInputDto();

        Mockito.when(mockValidator.validate(dto)).thenReturn(new HashSet<>());

        Department d = new Department();
        Mockito.when(departmentRepository.findByCode(dto.getDepartmentCode())).thenReturn(Optional.of(d));

        Designation title = new Designation();
        Mockito.when(designationRepository.findByTitle(dto.getTitle())).thenReturn(Optional.of(title));

        Teacher dbTeacher = DataHelper.validPersistableTeacher1(title, d);
        dbTeacher.setId(101L);
        Mockito.when(teacherRepository.findById(101L)).thenReturn(Optional.of(dbTeacher));

        ArgumentCaptor<Teacher> ac = ArgumentCaptor.forClass(Teacher.class);
        Mockito.when(teacherRepository.save(ac.capture())).thenReturn(null);

        teacherService.updateAll(101L, dto);

        Teacher t = ac.getValue();
        assertTeacherDataEquals(t, dto, d, title);

        Mockito.verify(teacherRepository, Mockito.times(1)).flush();
    }

    @Test
    void updateProvided_success() {
        TeacherPatchInputDto patchDto = new TeacherPatchInputDto();
        patchDto.setFullName("Updated");
        patchDto.setDepartmentCode("QQQ");
        patchDto.setTitle("Title");

        Teacher mockDBTeacher = createMockDBTeacher();
        Mockito.when(teacherRepository.findById(3L)).thenReturn(Optional.of(mockDBTeacher));

        Designation d = new Designation();
        d.setId(10L);
        d.setTitle("Title");

        Mockito.when(designationRepository.findByTitle("Title")).thenReturn(Optional.of(d));

        Department dept = new Department();
        dept.setId(11L);
        dept.setCode("QQQ");
        dept.setName("Quality Quick Quite");

        Mockito.when(departmentRepository.findByCode("QQQ")).thenReturn(Optional.of(dept));

        ArgumentCaptor<Teacher> ac = ArgumentCaptor.forClass(Teacher.class);
        Mockito.when(teacherRepository.save(ac.capture())).thenReturn(null);

        teacherService.updateProvided(3L, patchDto);

        Teacher arg = ac.getValue();
        Assertions.assertEquals(patchDto.getFullName(), arg.getFullName());
        Assertions.assertEquals(patchDto.getDepartmentCode(), arg.getDepartment().getCode());
        Assertions.assertEquals(patchDto.getTitle(), arg.getDesignation().getTitle());

        Assertions.assertNull(patchDto.getAddress());
        Assertions.assertEquals(mockDBTeacher.getAddress(), arg.getAddress());

        Assertions.assertNull(patchDto.getEmail());
        Assertions.assertEquals(mockDBTeacher.getEmail(), arg.getEmail());

        Assertions.assertNull(patchDto.getContactNumber());
        Assertions.assertEquals(mockDBTeacher.getContactNumber(), arg.getContactNumber());

        Assertions.assertNull(patchDto.getAssignedCredit());
        Assertions.assertEquals(mockDBTeacher.getAssignedCredit(), arg.getAssignedCredit());

        Mockito.verify(teacherRepository, Mockito.times(1)).flush();
    }

    private void assertTeacherDataEquals(Teacher t, TeacherDto dto, Department expectedDept, Designation expectedTitle) {

        Assertions.assertEquals(dto.getFullName(), t.getFullName());
        Assertions.assertEquals(dto.getAddress(), t.getAddress());
        Assertions.assertEquals(dto.getEmail(), t.getEmail());
        Assertions.assertEquals(dto.getContactNumber(), t.getContactNumber());
        Assertions.assertEquals(dto.getAssignedCredit(), t.getAssignedCredit());
        Assertions.assertSame(expectedDept, t.getDepartment());
        Assertions.assertSame(expectedTitle, t.getDesignation());
    }

    private Teacher createMockDBTeacher() {
        Designation designation = DataHelper.validPersistableDesignation();
        designation.setId(1L);

        Department department = DataHelper.validPersistableDepartment1();
        department.setId(2L);

        Teacher dbTeacher = DataHelper.validPersistableTeacher1(designation, department);
        dbTeacher.setId(3L);

        return dbTeacher;
    }
}
