package me.ronygomes.ums.api.service;

import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Education;
import me.ronygomes.ums.api.model.Grade;
import me.ronygomes.ums.api.model.Student;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import me.ronygomes.ums.api.repository.StudentRepository;
import me.ronygomes.ums.api.validator.EducationValidator;
import me.ronygomes.ums.api.validator.StudentValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private StudentValidator studentValidator;

    @Mock
    private EducationValidator educationValidator;

    @Mock
    private ExceptionHelper exceptionHelper;

    private StudentService service;

    @BeforeEach
    void setup() {
        service = new StudentService(studentRepository, departmentRepository,
                studentValidator, educationValidator, exceptionHelper);
    }

    @Test
    void testFindById() {
        Student s = new Student();
        Mockito.when(studentRepository.findWithEducationById(1L)).thenReturn(Optional.of(s));
        Assertions.assertSame(s, service.findById(1L));
    }

    @Test
    void testFindByIdFailed() {
        Mockito.when(studentRepository.findWithEducationById(1L)).thenReturn(Optional.empty());
        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> service.findById(1L));

        Assertions.assertNotNull(ex);
        Assertions.assertEquals(ExceptionType.ENTITY_NOT_FOUND, ex.getExceptionType());
        Assertions.assertEquals("Student with id '1' not found", ex.getErrorDetails());
        Assertions.assertEquals(0, ex.getErrors().size());
    }

    @Test
    void testCreate() {
        ArgumentCaptor<Student> ac = ArgumentCaptor.forClass(Student.class);
        Mockito.doAnswer(i -> {
            Student a = i.getArgument(0);
            a.setId(500L);
            return null;
        }).when(studentRepository).save(ac.capture());

        Student s = new Student();
        s.setDepartmentCode("ABC");
        Department d = new Department();
        Mockito.when(departmentRepository.findByCode("ABC")).thenReturn(Optional.of(d));

        long newId = service.create(s);

        Assertions.assertEquals(500, newId);
        Assertions.assertSame(s, ac.getValue());
        Assertions.assertSame(d, ac.getValue().getDepartment());
        Mockito.verify(studentValidator, Mockito.times(1)).validate(Mockito.any(Student.class), Mockito.any());
        Mockito.verify(exceptionHelper, Mockito.times(1)).throwErrorIfValidationError(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void testUpdate() {
        ArgumentCaptor<Student> ac = ArgumentCaptor.forClass(Student.class);
        Mockito.when(studentRepository.save(ac.capture())).thenReturn(null);

        Student s = new Student();
        s.setDepartmentCode("ABC");
        Department d = new Department();
        Mockito.when(departmentRepository.findByCode("ABC")).thenReturn(Optional.of(d));

        Student mockDBStudent = new Student();
        mockDBStudent.setId(7L);
        Mockito.when(studentRepository.findById(9L)).thenReturn(Optional.of(mockDBStudent));

        service.updateAll(9L, s);

        Assertions.assertSame(s, ac.getValue());
        Assertions.assertEquals(7, ac.getValue().getId());
        Assertions.assertSame(d, ac.getValue().getDepartment());
        Mockito.verify(studentValidator, Mockito.times(1)).validate(Mockito.any(Student.class), Mockito.any());
        Mockito.verify(exceptionHelper, Mockito.times(1)).throwErrorIfValidationError(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void testUpdatePatch() {
        ArgumentCaptor<Student> ac = ArgumentCaptor.forClass(Student.class);
        Mockito.when(studentRepository.save(ac.capture())).thenReturn(null);

        Student input = new Student();
        input.setDepartmentCode("ABC");
        Department d = new Department();
        Mockito.when(departmentRepository.findByCode("ABC")).thenReturn(Optional.of(d));

        Student mockDBStudent = Mockito.spy(Student.class);
        mockDBStudent.setId(2L);
        Mockito.when(studentRepository.findById(1L)).thenReturn(Optional.of(mockDBStudent));
        Mockito.when(mockDBStudent.getDepartmentCode()).thenReturn("ABC");

        service.updateProvided(1L, input);

        Assertions.assertSame(mockDBStudent, ac.getValue());
        Assertions.assertEquals(2L, ac.getValue().getId());
        Assertions.assertSame(d, ac.getValue().getDepartment());
        Mockito.verify(studentValidator, Mockito.times(1)).validate(Mockito.any(Student.class), Mockito.any());
        Mockito.verify(exceptionHelper, Mockito.times(1)).throwErrorIfValidationError(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(mockDBStudent, Mockito.times(1)).merge(input);
    }

    @Test
    void testDelete() {
        ArgumentCaptor<Student> ac = ArgumentCaptor.forClass(Student.class);
        Mockito.doNothing().when(studentRepository).delete(ac.capture());

        Student s = new Student();
        Mockito.when(studentRepository.findById(55L)).thenReturn(Optional.of(s));

        service.delete(55L);
        Assertions.assertSame(s, ac.getValue());
    }

    @Test
    void testEducationAdd() {
        Student s = new Student();
        s.setDepartmentCode("ABC");
        Assertions.assertEquals(0, s.getEducations().size());

        Mockito.when(studentRepository.findWithEducationById(1L)).thenReturn(Optional.of(s));
        Mockito.when(departmentRepository.findByCode("ABC")).thenReturn(Optional.of(new Department()));

        Education e = new Education();
        service.addEducation(1L, e);

        Assertions.assertEquals(1, s.getEducations().size());
        Assertions.assertSame(e, s.getEducations().get(0));
        Mockito.verify(educationValidator, Mockito.times(1)).validate(Mockito.eq(e), Mockito.any());
        Mockito.verify(studentRepository, Mockito.times(1)).save(s);
    }

    @Test
    void testEducationDelete() {
        Student student = createMockStudent();

        Mockito.when(studentRepository.findWithEducationById(2L)).thenReturn(Optional.of(student));
        Mockito.when(departmentRepository.findByCode("CODE-1")).thenReturn(Optional.of(new Department()));

        service.deleteEducation(2L, 100L);

        Mockito.verify(educationValidator, Mockito.never()).validate(Mockito.any(), Mockito.any());
        Mockito.verify(studentRepository, Mockito.times(1)).save(student);
        Assertions.assertEquals(1, student.getEducations().size());
        Assertions.assertEquals(101, student.getEducations().get(0).getId());
    }

    @Test
    void testEducationUpdate() {
        Education e = new Education();

        Student s = createMockStudent();
        Mockito.when(studentRepository.findWithEducationById(2L)).thenReturn(Optional.of(s));
        Mockito.when(departmentRepository.findByCode("CODE-1")).thenReturn(Optional.of(new Department()));

        Assertions.assertNull(e.getId());
        service.updateEducation(2L, 101L, e);

        Assertions.assertEquals(2, s.getEducations().size());
        Assertions.assertSame(e, s.getEducations().get(1));
        Assertions.assertEquals(101L, e.getId());

        Mockito.verify(educationValidator, Mockito.times(1)).validate(Mockito.eq(e), Mockito.any());
        Mockito.verify(studentRepository, Mockito.times(1)).save(s);
    }

    @Test
    void testEducationPatchUpdate() {
        Education e = new Education();
        e.setGrade(Grade.F);

        Student s = createMockStudent();
        Assertions.assertEquals(Grade.A_PLUS, s.getEducations().get(1).getGrade());
        Mockito.when(studentRepository.findWithEducationById(2L)).thenReturn(Optional.of(s));
        Mockito.when(departmentRepository.findByCode("CODE-1")).thenReturn(Optional.of(new Department()));

        service.updatePatchEducation(2L, 101L, e);

        Assertions.assertEquals(2, s.getEducations().size());
        Assertions.assertEquals(Grade.F, s.getEducations().get(1).getGrade());

        Education dbEducation = s.getEducations().get(1);
        Mockito.verify(educationValidator, Mockito.times(1)).validate(Mockito.eq(dbEducation), Mockito.any());
        Mockito.verify(studentRepository, Mockito.times(1)).save(s);
    }

    private Student createMockStudent() {
        Department d = DataHelper.validPersistableDepartment1();
        d.setId(1L);

        Student student = DataHelper.validPersistableStudent1(d);
        student.setId(2L);
        Assertions.assertEquals(2, student.getEducations().size());

        student.getEducations().get(0).setId(100L);
        student.getEducations().get(1).setId(101L);
        return student;
    }
}
