package me.ronygomes.ums.api.exception;

import java.util.ArrayList;
import java.util.List;

public class UmsDataException extends RuntimeException {

    private final ExceptionType exceptionType;
    private final String errorDetails;
    private final List<ErrorMessage> errors;

    public UmsDataException(ExceptionType exceptionType, String errorDetails) {
        this(exceptionType, errorDetails, new ArrayList<>());
    }

    public UmsDataException(ExceptionType exceptionType,
                            String errorDetails,
                            List<ErrorMessage> errors) {

        this.exceptionType = exceptionType;
        this.errorDetails = errorDetails;
        this.errors = errors;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public List<ErrorMessage> getErrors() {
        return errors;
    }
}
