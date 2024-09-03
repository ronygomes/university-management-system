package me.ronygomes.ums.api.service;

import me.ronygomes.ums.api.dto.EnrollmentDto;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.*;
import me.ronygomes.ums.api.repository.CourseRepository;
import me.ronygomes.ums.api.repository.EnrollmentRepository;
import me.ronygomes.ums.api.repository.StudentRepository;
import me.ronygomes.ums.api.validator.EnrollmentValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private EnrollmentValidator enrollmentValidator;

    @Mock
    private ExceptionHelper exceptionHelper;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentRepository studentRepository;

    private EnrollmentService service;

    @BeforeEach
    void setup() {
        service = new EnrollmentService(enrollmentRepository, enrollmentValidator,
                courseRepository, studentRepository, exceptionHelper);
    }

    @Test
    void testFindByIdSuccess() {
        Enrollment e = mockEnrollment();
        Mockito.when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(e));

        EnrollmentDto dto = service.findById(1L);
        assertDataEquals(e, dto);
    }

    @Test
    void testFindByIdFailed() {
        Mockito.when(enrollmentRepository.findById(1L)).thenReturn(Optional.empty());

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> service.findById(1L));
        Assertions.assertEquals(ExceptionType.ENTITY_NOT_FOUND, ex.getExceptionType());
        Assertions.assertEquals("Enrollment with id=1 not found", ex.getErrorDetails());
        Assertions.assertEquals(0, ex.getErrors().size());
    }

    @Test
    void testCreateSuccess() {
        Date now = new Date();
        var dto = createMockEnrollmentDto(now);

        ArgumentCaptor<Enrollment> ac = ArgumentCaptor.forClass(Enrollment.class);
        Mockito.doAnswer(i -> {
            Enrollment e = i.getArgument(0);
            e.setId(100L);
            return null;
        }).when(enrollmentRepository).save(ac.capture());

        Course c = new Course();
        Mockito.when(courseRepository.findById(2L)).thenReturn(Optional.of(c));

        Student s = new Student();
        Mockito.when(studentRepository.findById(3L)).thenReturn(Optional.of(s));

        long newId = service.create(dto);
        Assertions.assertEquals(100, newId);

        Enrollment r = ac.getValue();
        Assertions.assertSame(c, r.getCourse());
        Assertions.assertSame(s, r.getStudent());
        Assertions.assertEquals(EnrollmentStatus.PASSED, r.getStatus());
        Assertions.assertEquals(Grade.A, r.getGrade());
        Assertions.assertSame(now, r.getEnrollmentDate());

        Mockito.verify(enrollmentValidator, Mockito.times(1)).validate(Mockito.any(), Mockito.any());
        Mockito.verify(exceptionHelper, Mockito.times(1)).throwErrorIfValidationError(Mockito.any(), Mockito.any());
    }

    @Test
    void testUpdateSuccess() {
        Date now = new Date();
        var dto = createMockEnrollmentDto(now);

        ArgumentCaptor<Enrollment> ac = ArgumentCaptor.forClass(Enrollment.class);
        Mockito.when(enrollmentRepository.save(ac.capture())).thenReturn(null);

        Course c = new Course();
        Mockito.when(courseRepository.findById(2L)).thenReturn(Optional.of(c));

        Student s = new Student();
        Mockito.when(studentRepository.findById(3L)).thenReturn(Optional.of(s));

        Enrollment  e = new Enrollment();
        Mockito.when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(e));

        service.update(100L, dto);

        Enrollment r = ac.getValue();
        Assertions.assertSame(c, r.getCourse());
        Assertions.assertSame(s, r.getStudent());
        Assertions.assertEquals(EnrollmentStatus.PASSED, r.getStatus());
        Assertions.assertEquals(Grade.A, r.getGrade());
        Assertions.assertSame(now, r.getEnrollmentDate());
        Assertions.assertEquals(100L, r.getId());

        Mockito.verify(enrollmentValidator, Mockito.times(1)).validate(Mockito.any(), Mockito.any());
        Mockito.verify(exceptionHelper, Mockito.times(1)).throwErrorIfValidationError(Mockito.any(), Mockito.any());
    }

    @Test
    void testUpdateProvidedSuccess() {
        Student dbS = new Student();
        dbS.setId(10L);

        Course dbC = new Course();
        dbC.setId(20L);

        Enrollment dbE = new Enrollment();
        dbE.setId(500L);
        dbE.setStudent(dbS);
        dbE.setCourse(dbC);

        Mockito.when(enrollmentRepository.findById(99L)).thenReturn(Optional.of(dbE));
        ArgumentCaptor<Enrollment> ac = ArgumentCaptor.forClass(Enrollment.class);
        Mockito.when(enrollmentRepository.save(ac.capture())).thenReturn(null);

        Mockito.when(studentRepository.findById(10L)).thenReturn(Optional.of(dbS));
        Mockito.when(courseRepository.findById(20L)).thenReturn(Optional.of(dbC));

        Assertions.assertNull(dbE.getStatus());
        EnrollmentDto input = new EnrollmentDto();
        input.setStatus(EnrollmentStatus.PASSED);
        service.updateProvided(99L, input);

        Enrollment e = ac.getValue();
        Assertions.assertEquals(99L, e.getId());
        Assertions.assertEquals(EnrollmentStatus.PASSED, e.getStatus());
        Assertions.assertSame(dbS, e.getStudent());
        Assertions.assertSame(dbC, e.getCourse());
        Assertions.assertNull(e.getEnrollmentDate());
        Assertions.assertNull(e.getGrade());
    }

    @Test
    void testDelete() {
        ArgumentCaptor<Enrollment> ac = ArgumentCaptor.forClass(Enrollment.class);
        Mockito.doNothing().when(enrollmentRepository).delete(ac.capture());

        Enrollment  e = new Enrollment();
        e.setId(500L);
        Mockito.when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(e));

        service.delete(100L);
        Assertions.assertSame(e, ac.getValue());
    }

    private void assertDataEquals(Enrollment e, EnrollmentDto dto) {
        Assertions.assertEquals(e.getId(), dto.getId());
        Assertions.assertEquals(e.getCourse().getId(), dto.getCourseId());
        Assertions.assertEquals(e.getStudent().getId(), dto.getStudentId());
        Assertions.assertEquals(e.getGrade(), dto.getGrade());
        Assertions.assertEquals(e.getStatus(), dto.getStatus());
        Assertions.assertEquals(e.getEnrollmentDate(), dto.getEnrollmentDate());
    }

    private Enrollment mockEnrollment() {
        Student s = new Student();
        s.setId(3L);

        Course c = new Course();
        c.setId(2L);

        Enrollment e = DataHelper.validPersistableEnrollment1(s, c);
        e.setId((long) 1);

        return e;
    }

    private EnrollmentDto createMockEnrollmentDto(Date enrollmentDate) {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setStatus(EnrollmentStatus.PASSED);
        dto.setId(1L);
        dto.setCourseId(2L);
        dto.setStudentId(3L);
        dto.setEnrollmentDate(enrollmentDate);
        dto.setGrade(Grade.A);

        return dto;
    }
}
