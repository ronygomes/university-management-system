package me.ronygomes.ums.api.validator.annotation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import me.ronygomes.ums.api.config.TestContextConfig;
import me.ronygomes.ums.api.model.EmailData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ContextConfiguration(classes = {TestContextConfig.class})
public class EmailTest {

    @Autowired
    private Validator validator;

    @Test
    void testValidEmail() {
        EmailData e = new EmailData("john@example.com");
        Set<ConstraintViolation<EmailData>> errors = validator.validate(e);
        Assertions.assertEquals(0, errors.size());
    }

    @Test
    void testInValidEmail() {
        EmailData e = new EmailData("example.com");
        Set<ConstraintViolation<EmailData>> errorsSet = validator.validate(e);
        Assertions.assertEquals(1, errorsSet.size());

        List<ConstraintViolation<EmailData>> errors = new ArrayList<>(errorsSet);
        Assertions.assertEquals("invalid email format", errors.get(0).getMessage());
        Assertions.assertEquals("example.com", errors.get(0).getInvalidValue());
        Assertions.assertEquals("email", errors.get(0).getPropertyPath().toString());
    }

    @Test
    void testCustomMessage() {
        EmailData e = new EmailData("john@example.com");
        e.setEmail2("example.com");
        Set<ConstraintViolation<EmailData>> errorsSet = validator.validate(e);
        Assertions.assertEquals(1, errorsSet.size());

        List<ConstraintViolation<EmailData>> errors = new ArrayList<>(errorsSet);
        Assertions.assertEquals("Custom Message", errors.get(0).getMessage());
        Assertions.assertEquals("example.com", errors.get(0).getInvalidValue());
        Assertions.assertEquals("email2", errors.get(0).getPropertyPath().toString());
    }
}
