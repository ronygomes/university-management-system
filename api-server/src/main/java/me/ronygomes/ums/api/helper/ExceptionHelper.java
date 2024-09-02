package me.ronygomes.ums.api.helper;

import jakarta.validation.ConstraintViolationException;
import me.ronygomes.ums.api.exception.ErrorMessage;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExceptionHelper {

    private static final Pattern DATABASE_INTEGRITY_EXCEPTION_MESSAGE_PATTERN = Pattern.compile("Detail: ([^]]+).*");

    public List<ErrorMessage> extractConstraintViolation(Throwable throwable) {
        List<ErrorMessage> errors = new ArrayList<>();
        if (throwable instanceof TransactionSystemException) {
            while (throwable != null) {
                if (throwable.getClass() == ConstraintViolationException.class) {
                    ConstraintViolationException ce = (ConstraintViolationException) throwable;

                    ce.getConstraintViolations().forEach(cv -> {
                        errors.add(new ErrorMessage(cv.getPropertyPath().toString(), cv.getMessage()));
                    });

                    return errors;
                }
                throwable = throwable.getCause();
            }
        } else if (throwable instanceof DataIntegrityViolationException) {
            Matcher m = DATABASE_INTEGRITY_EXCEPTION_MESSAGE_PATTERN
                    .matcher(NestedExceptionUtils.getMostSpecificCause(throwable).getMessage());

            if (m.find()) {
                errors.add(new ErrorMessage("*", m.group(1)));
            }

            return errors;
        }

        throw new RuntimeException("Not a ConstraintViolation or DataIntegrityViolationException");
    }

    public void throwErrorIfValidationError(BindingResult result, String message, String... ignoreFields) {
        if (!result.hasErrors()) {
            return;
        }

        List<ErrorMessage> errors = new ArrayList<>();
        result.getFieldErrors().forEach(fe -> {
            if (!Arrays.asList(ignoreFields).contains(fe.getField())) {
                errors.add(new ErrorMessage(fe.getField(), fe.getDefaultMessage()));
            }
        });

        throw new UmsDataException(ExceptionType.DATA_VALIDATION_FAILED, message, errors);
    }
}
