package me.ronygomes.ums.api.validator;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Student;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class StudentValidatorTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private Validator beanValidator;

    private StudentValidator validator;

    @BeforeEach
    void setup() {
        validator = new StudentValidator(departmentRepository, beanValidator);
    }

    @Test
    void testSupportsStudent() {
        Assertions.assertTrue(validator.supports(Student.class));
    }

    @Test
    void beanValidationTriggers() {
        Student s = new Student();
        s.setDepartmentCode("ABC");
        Mockito.when(departmentRepository.findByCode("ABC")).thenReturn(Optional.of(new Department()));

        BindingResult errors = new BeanPropertyBindingResult(s, "student");
        Assertions.assertDoesNotThrow(() -> validator.validate(s, errors));

        Mockito.verify(beanValidator, Mockito.times(1)).validate(Mockito.eq(s));
    }

    @Test
    void testDepartmentCodeValidationForNull() {
        Student s = new Student();
        s.setDepartmentCode(null);

        BindingResult errors = new BeanPropertyBindingResult(s, "student");
        validator.validate(s, errors);

        Assertions.assertEquals(1, errors.getErrorCount());
        Assertions.assertNotNull(errors.getFieldError("departmentCode"));
        Assertions.assertEquals("must not be null", errors.getFieldError("departmentCode").getDefaultMessage());

        Mockito.verify(departmentRepository, Mockito.never()).findByCode(Mockito.any());
        Mockito.verify(beanValidator, Mockito.times(1)).validate(Mockito.eq(s));
    }

    @Test
    void testDepartmentCodeValidationNonExistentInDatabase() {
        Student s = new Student();
        s.setDepartmentCode("ABC");

        Mockito.when(departmentRepository.findByCode("ABC")).thenReturn(Optional.empty());
        BindingResult errors = new BeanPropertyBindingResult(s, "student");
        validator.validate(s, errors);

        Assertions.assertEquals(1, errors.getErrorCount());
        Assertions.assertNotNull(errors.getFieldError("departmentCode"));
        Assertions.assertEquals("department not found", errors.getFieldError("departmentCode").getDefaultMessage());
        Mockito.verify(beanValidator, Mockito.times(1)).validate(Mockito.eq(s));
    }
}
