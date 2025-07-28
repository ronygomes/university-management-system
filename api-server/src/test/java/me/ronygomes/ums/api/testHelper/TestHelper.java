package me.ronygomes.ums.api.testHelper;

import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.MediaType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class TestHelper {
    public static final MediaType HAL_FORMS = new MediaType("application", "prs.hal-forms+json");

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

    public static boolean isEnumFieldStoredAsString(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            Enumerated annotation = field.getAnnotation(Enumerated.class);
            if (Objects.isNull(annotation)) {
                return false;
            }

            return annotation.value().equals(EnumType.STRING);

        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(String.format("Field '%s' doesn't exists in %s",
                    fieldName, clazz.getCanonicalName()));
        }
    }

    public static boolean isConvertPresent(Class<?> clazz, String fieldName, Class<?> converterType) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            Convert annotation = field.getAnnotation(Convert.class);
            if (Objects.isNull(annotation)) {
                return false;
            }

            return annotation.converter().equals(converterType);

        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(String.format("Field '%s' doesn't exists in %s",
                    fieldName, clazz.getCanonicalName()));
        }
    }

    public static Method[] getPublicMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .toArray(Method[]::new);
    }
}
