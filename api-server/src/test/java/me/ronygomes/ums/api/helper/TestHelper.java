package me.ronygomes.ums.api.helper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.Set;

public class TestHelper {

    public static Set<ConstraintViolation<?>> extractConstraintViolation(Throwable throwable) {
        while (throwable != null) {
            if (throwable.getClass() == ConstraintViolationException.class) {
                ConstraintViolationException ce = (ConstraintViolationException) throwable;
                return ce.getConstraintViolations();
            }
            throwable = throwable.getCause();
        }

        throw new RuntimeException("Not a ConstraintViolation");
    }
}
