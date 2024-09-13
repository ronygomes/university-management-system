package me.ronygomes.ums.api.validator.annotation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import me.ronygomes.ums.api.config.TestContextConfig;
import me.ronygomes.ums.api.model.ContactNumberData;
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
public class ContactNumberTest {

    @Autowired
    private Validator validator;

    @Test
    void testValidContactNumber() {
        ContactNumberData c = new ContactNumberData("+1111111111111");
        Set<ConstraintViolation<ContactNumberData>> errors = validator.validate(c);
        Assertions.assertEquals(0, errors.size());
    }

    @Test
    void testInValidContactNumber() {
        ContactNumberData c = new ContactNumberData("+1x");
        Set<ConstraintViolation<ContactNumberData>> errorSet = validator.validate(c);
        Assertions.assertEquals(1, errorSet.size());

        List<ConstraintViolation<ContactNumberData>> errors = new ArrayList<>(errorSet);
        Assertions.assertEquals("invalid contact number format", errors.get(0).getMessage());
        Assertions.assertEquals("+1x", errors.get(0).getInvalidValue());
        Assertions.assertEquals("contactNumber", errors.get(0).getPropertyPath().toString());
    }

    @Test
    void testCustomMessage() {
        ContactNumberData c = new ContactNumberData("+1111111111111");
        c.setContactNumber2("+1x");
        Set<ConstraintViolation<ContactNumberData>> errorSet = validator.validate(c);
        Assertions.assertEquals(1, errorSet.size());

        List<ConstraintViolation<ContactNumberData>> errors = new ArrayList<>(errorSet);
        Assertions.assertEquals("Custom Message", errors.get(0).getMessage());
        Assertions.assertEquals("+1x", errors.get(0).getInvalidValue());
        Assertions.assertEquals("contactNumber2", errors.get(0).getPropertyPath().toString());
    }
}
