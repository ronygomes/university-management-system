package me.ronygomes.ums.api.exception;

import java.util.ArrayList;
import java.util.List;

public class UmsServiceException extends RuntimeException {

    private final ServiceErrorType serviceErrorType;
    private final String errorDetails;
    private final List<ErrorMessage> errors;

    public UmsServiceException(ServiceErrorType serviceErrorType, String errorDetails) {
        this(serviceErrorType, errorDetails, new ArrayList<>());
    }

    public UmsServiceException(ServiceErrorType serviceErrorType,
                               String errorDetails,
                               List<ErrorMessage> errors) {

        this.serviceErrorType = serviceErrorType;
        this.errorDetails = errorDetails;
        this.errors = errors;
    }

    public ServiceErrorType getServiceErrorType() {
        return serviceErrorType;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public List<ErrorMessage> getErrors() {
        return errors;
    }
}
