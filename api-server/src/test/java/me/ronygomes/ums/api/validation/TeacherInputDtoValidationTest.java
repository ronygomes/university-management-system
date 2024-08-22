package me.ronygomes.ums.api.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import me.ronygomes.ums.api.config.TestContextConfig;
import me.ronygomes.ums.api.dto.TeacherDto;
import me.ronygomes.ums.api.helper.DataHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

@SpringBootTest
@ContextConfiguration(classes = {TestContextConfig.class})
public class TeacherInputDtoValidationTest {

    @Autowired
    private Validator validator;

    private TeacherDto valid;

    @BeforeEach
    void setup() {
        Assertions.assertNotNull(validator);
        valid = DataHelper.validTeacherInputDto();

        Assertions.assertEquals(0, validator.validate(valid).size());
    }

    @Test
    void testValidate_fullName() {
        valid.setFullName(null);
        assertViolation(validator.validate(valid), "fullName", "must not be null", null);

        valid.setFullName("");
        assertViolation(validator.validate(valid), "fullName", "size must be between 1 and 200", "");

        valid.setFullName("a".repeat(201));
        assertViolation(validator.validate(valid), "fullName", "size must be between 1 and 200", "a".repeat(201));
    }

    @Test
    void testValidate_address() {
        valid.setAddress(null);
        Assertions.assertEquals(0, validator.validate(valid).size());

        valid.setAddress("");
        Assertions.assertEquals(0, validator.validate(valid).size());

        valid.setAddress("a".repeat(1001));
        assertViolation(validator.validate(valid), "address", "size must be between 0 and 1000", "a".repeat(1001));
    }

    @Test
    void testValidate_email() {
        valid.setEmail(null);
        assertViolation(validator.validate(valid), "email", "must not be null", null);

        valid.setEmail("a@b.co");
        Assertions.assertEquals(0, validator.validate(valid).size());

        String validEmailExceedingMaxLen = "a".repeat(95) + "@a.com";
        valid.setEmail(validEmailExceedingMaxLen);
        assertViolation(validator.validate(valid), "email", "size must be between 5 and 100", validEmailExceedingMaxLen);

        valid.setEmail("a@b");
        var errors = new ArrayList<>(validator.validate(valid).stream()
                .sorted(Comparator.comparing(ConstraintViolation::getMessage)).toList());

        Assertions.assertEquals(2, errors.size());
        assertViolation(Set.of(errors.get(0)), "email", "invalid email format", "a@b");
        assertViolation(Set.of(errors.get(1)), "email", "size must be between 5 and 100", "a@b");

        valid.setEmail("a".repeat(10));
        assertViolation(validator.validate(valid), "email", "invalid email format", "a".repeat(10));
    }

    @Test
    void testValidate_contactNumber() {
        valid.setContactNumber(null);
        Assertions.assertEquals(0, validator.validate(valid).size());

        valid.setContactNumber("");
        assertViolation(validator.validate(valid), "contactNumber", "invalid contact number format", "");

        valid.setContactNumber("+11111111111111");
        assertViolation(validator.validate(valid), "contactNumber", "invalid contact number format", "+11111111111111");
    }

    @Test
    void testValidate_assignedCredit() {
        valid.setAssignedCredit(-1);
        assertViolation(validator.validate(valid), "assignedCredit", "must be greater than or equal to 0", -1f);

        valid.setAssignedCredit(101);
        assertViolation(validator.validate(valid), "assignedCredit", "must be less than or equal to 100", 101f);
    }

    @Test
    void testValidate_title() {
        valid.setTitle(null);
        assertViolation(validator.validate(valid), "title", "must not be null", null);

        valid.setTitle("");
        assertViolation(validator.validate(valid), "title", "size must be between 1 and 100", "");

        String value = "a".repeat(101);
        valid.setTitle(value);
        assertViolation(validator.validate(valid), "title", "size must be between 1 and 100", value);
    }

    @Test
    void testValidate_departmentCode() {
        valid.setDepartmentCode(null);
        assertViolation(validator.validate(valid), "departmentCode", "must not be null", null);

        valid.setDepartmentCode("");
        assertViolation(validator.validate(valid), "departmentCode", "size must be between 1 and 10", "");

        String value = "a".repeat(11);
        valid.setDepartmentCode(value);
        assertViolation(validator.validate(valid), "departmentCode", "size must be between 1 and 10", value);
    }

    private void assertViolation(Set<ConstraintViolation<TeacherDto>> errors,
                                 String field, String message, Object expectedValue) {
        Assertions.assertEquals(1, errors.size());

        for (ConstraintViolation<TeacherDto> violation : errors) {
            Assertions.assertEquals(field, violation.getPropertyPath().toString());
            Assertions.assertEquals(message, violation.getMessage());

            if (Objects.isNull(expectedValue)) {
                Assertions.assertNull(violation.getInvalidValue());
            } else {
                Assertions.assertEquals(expectedValue, violation.getInvalidValue());
            }
        }
    }
}
