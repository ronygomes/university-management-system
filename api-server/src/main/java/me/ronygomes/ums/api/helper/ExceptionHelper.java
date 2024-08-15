package me.ronygomes.ums.api.helper;

import jakarta.validation.ConstraintViolationException;
import me.ronygomes.ums.api.exception.ErrorMessage;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;

import java.util.ArrayList;
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
}
