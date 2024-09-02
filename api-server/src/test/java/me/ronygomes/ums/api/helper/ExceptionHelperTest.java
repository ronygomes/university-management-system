package me.ronygomes.ums.api.helper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import me.ronygomes.ums.api.exception.ErrorMessage;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ContextConfiguration(classes = {ExceptionHelperTest.ContextConfig.class})
public class ExceptionHelperTest {

    @Autowired
    Validator validator;

    private ExceptionHelper helper;

    @BeforeEach
    void setup() {
        this.helper = new ExceptionHelper();
    }

    @Test
    void testThrowsExceptionForUnknownException() {
        Throwable t = new IllegalArgumentException("Ignore");
        Throwable rt = Assertions.assertThrows(RuntimeException.class, () -> helper.extractConstraintViolation(t));
        Assertions.assertEquals("Not a ConstraintViolation or DataIntegrityViolationException", rt.getMessage());
    }

    @Test
    void testConstrainViolationException() {
        Data d = new Data();
        d.setText("abcdefg");

        Set<ConstraintViolation<Data>> constrains = validator.validate(d);
        ConstraintViolationException cve = new ConstraintViolationException("CVE", constrains);
        TransactionSystemException tse = new TransactionSystemException("TSE", cve);

        List<ErrorMessage> errors = helper.extractConstraintViolation(tse);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("text", errors.get(0).getField());
        Assertions.assertEquals("size must be between 1 and 5", errors.get(0).getMessage());
    }

    @Test
    void testDataIntegrityViolationException() {

        DataIntegrityViolationException die = new DataIntegrityViolationException("Detail: xxx");
        List<ErrorMessage> errors = helper.extractConstraintViolation(die);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("*", errors.get(0).getField());
        Assertions.assertEquals("xxx", errors.get(0).getMessage());
    }

    @Test
    void testThrowErrorIfValidationErrorDoesNothingIfNoError() {
        BindingResult result = Mockito.mock(BindingResult.class);
        Mockito.when(result.hasErrors()).thenReturn(false);

        Assertions.assertDoesNotThrow(() -> helper.throwErrorIfValidationError(result, null));
    }

    @Test
    void testThrowErrorIfValidationErrorSuccess() {
        BindingResult result = Mockito.mock(BindingResult.class);
        Mockito.when(result.hasErrors()).thenReturn(true);

        FieldError fe1 = new FieldError("parent1", "field1", "default message 1");
        FieldError fe2 = new FieldError("parent1", "field2", "default message 2");
        List<FieldError> errors = Arrays.asList(fe1, fe2);
        Mockito.when(result.getFieldErrors()).thenReturn(errors);

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> helper.throwErrorIfValidationError(result, "abc"));
        Assertions.assertNotNull(ex);
        Assertions.assertEquals(ExceptionType.DATA_VALIDATION_FAILED, ex.getExceptionType());
        Assertions.assertEquals("abc", ex.getErrorDetails());
        Assertions.assertEquals(2, ex.getErrors().size());
        Assertions.assertEquals("field1", ex.getErrors().get(0).getField());
        Assertions.assertEquals("default message 1", ex.getErrors().get(0).getMessage());
        Assertions.assertEquals("field2", ex.getErrors().get(1).getField());
        Assertions.assertEquals("default message 2", ex.getErrors().get(1).getMessage());
    }

    @Test
    void testThrowErrorIfValidationErrorFiltering() {
        BindingResult result = Mockito.mock(BindingResult.class);
        Mockito.when(result.hasErrors()).thenReturn(true);

        FieldError fe1 = new FieldError("parent1", "field1", "default message 1");
        FieldError fe2 = new FieldError("parent1", "field2", "default message 2");
        List<FieldError> errors = Arrays.asList(fe1, fe2);
        Mockito.when(result.getFieldErrors()).thenReturn(errors);

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> helper.throwErrorIfValidationError(result, "abc", "field1"));
        Assertions.assertNotNull(ex);
        Assertions.assertEquals(ExceptionType.DATA_VALIDATION_FAILED, ex.getExceptionType());
        Assertions.assertEquals("abc", ex.getErrorDetails());
        Assertions.assertEquals(1, ex.getErrors().size());
        Assertions.assertEquals("field2", ex.getErrors().get(0).getField());
        Assertions.assertEquals("default message 2", ex.getErrors().get(0).getMessage());
    }

    @Configuration
    static class ContextConfig {
        @Bean
        public static LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }
}
