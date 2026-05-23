package me.ronygomes.ums.api.validator;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.dto.EnrollmentDto;
import me.ronygomes.ums.api.model.CourseSchedule;
import me.ronygomes.ums.api.model.Student;
import me.ronygomes.ums.api.repository.CourseScheduleRepository;
import me.ronygomes.ums.api.repository.StudentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EnrollmentValidatorTest {

    @Mock
    private Validator validator;

    @Mock
    private CourseScheduleRepository courseScheduleRepository;

    @Mock
    private StudentRepository studentRepository;

    private EnrollmentValidator enrollmentValidator;

    @BeforeEach
    void setup() {
        enrollmentValidator = new EnrollmentValidator(validator, courseScheduleRepository, studentRepository);
    }

    @Test
    void testSupports() {
        Assertions.assertTrue(enrollmentValidator.supports(EnrollmentDto.class));
    }

    @Test
    void testBeanValidationTriggersError() {
        EnrollmentDto dto = new EnrollmentDto();
        BindingResult errors = Mockito.mock(BindingResult.class);

        Mockito.when(errors.hasFieldErrors("courseScheduleId")).thenReturn(true);
        Mockito.when(errors.hasFieldErrors("studentId")).thenReturn(true);

        enrollmentValidator.validate(dto, errors);
        Mockito.verify(validator, Mockito.times(1)).validate(dto);
        Assertions.assertEquals(0, errors.getErrorCount());

        Mockito.verify(courseScheduleRepository, Mockito.never()).findById(Mockito.any());
        Mockito.verify(studentRepository, Mockito.never()).findById(Mockito.any());
    }

    @Test
    void testBeanValidationTriggersSuccess() {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setCourseScheduleId(1L);
        dto.setStudentId(2L);

        BindingResult errors = Mockito.mock(BindingResult.class);

        Mockito.when(errors.hasFieldErrors("courseScheduleId")).thenReturn(false);
        Mockito.when(errors.hasFieldErrors("studentId")).thenReturn(false);
        CourseSchedule openSchedule = new CourseSchedule();
        openSchedule.setEnrollmentOpen(true);
        Mockito.when(courseScheduleRepository.findById(1L)).thenReturn(Optional.of(openSchedule));
        Mockito.when(studentRepository.findById(2L)).thenReturn(Optional.of(new Student()));

        enrollmentValidator.validate(dto, errors);
        Mockito.verify(validator, Mockito.times(1)).validate(dto);

        Mockito.verify(courseScheduleRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(studentRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(errors, Mockito.never()).rejectValue(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void testRejectsWhenEnrollmentClosed() {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setCourseScheduleId(1L);
        dto.setStudentId(2L);

        BindingResult errors = Mockito.mock(BindingResult.class);

        Mockito.when(errors.hasFieldErrors("courseScheduleId")).thenReturn(false);
        Mockito.when(errors.hasFieldErrors("studentId")).thenReturn(false);
        CourseSchedule closedSchedule = new CourseSchedule();
        closedSchedule.setEnrollmentOpen(false);
        Mockito.when(courseScheduleRepository.findById(1L)).thenReturn(Optional.of(closedSchedule));
        Mockito.when(studentRepository.findById(2L)).thenReturn(Optional.of(new Student()));

        enrollmentValidator.validate(dto, errors);

        Mockito.verify(errors, Mockito.times(1)).rejectValue(
                "courseScheduleId", null, "enrollment is closed for this course schedule");
    }

    @Test
    void testBeanValidationTriggersFailed() {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setCourseScheduleId(1L);
        dto.setStudentId(2L);

        BindingResult errors = Mockito.mock(BindingResult.class);

        Mockito.when(errors.hasFieldErrors("courseScheduleId")).thenReturn(false);
        Mockito.when(errors.hasFieldErrors("studentId")).thenReturn(false);
        Mockito.when(courseScheduleRepository.findById(1L)).thenReturn(Optional.empty());
        Mockito.when(studentRepository.findById(2L)).thenReturn(Optional.empty());

        enrollmentValidator.validate(dto, errors);
        Mockito.verify(validator, Mockito.times(1)).validate(dto);

        Mockito.verify(courseScheduleRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(studentRepository, Mockito.times(1)).findById(Mockito.any());

        Mockito.verify(errors, Mockito.times(2)).rejectValue(
                Mockito.argThat(m -> m.equals("courseScheduleId") || m.equals("studentId")),
                Mockito.isNull(),
                Mockito.argThat(m -> m.equals("course schedule not found") || m.equals("student not found")));
    }
}
