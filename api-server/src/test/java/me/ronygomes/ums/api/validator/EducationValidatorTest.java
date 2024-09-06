package me.ronygomes.ums.api.validator;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.model.Education;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
public class EducationValidatorTest {

    @Mock
    private Validator validator;

    private EducationValidator cut;

    @BeforeEach
    void setup() {
        cut = new EducationValidator(validator);
    }

    @Test
    void testSupports() {
        Assertions.assertTrue(cut.supports(Education.class));
    }

    @Test
    void testBeanValidationInvocation() {
        Education e = new Education();
        cut.validate(e, new MapBindingResult(new HashMap<>(), "placeholder"));
        Mockito.verify(validator, Mockito.times(1)).validate(e);
    }
}
