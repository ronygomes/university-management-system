package me.ronygomes.ums.api.handler;

import jakarta.servlet.http.HttpServletRequest;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UmsDataException.class)
    public ResponseEntity<Problem> handleUmsServiceException(UmsDataException ex, HttpServletRequest request) {
        Problem problem = Problem.create()
                .withType(ex.getExceptionType().getDocumentationUrl())
                .withTitle(ex.getExceptionType().getTitle())
                .withDetail(ex.getErrorDetails())
                .withInstance(URI.create(request.getRequestURI()))
                .withProperties(m -> {
                    if (!ex.getErrors().isEmpty()) {
                        m.put("errors", ex.getErrors());
                    }
                });

        return ResponseEntity.status(statusFor(ex.getExceptionType())).body(problem);
    }

    private static HttpStatus statusFor(ExceptionType type) {
        return switch (type) {
            case DATA_VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
            case ENTITY_NOT_FOUND -> HttpStatus.NOT_FOUND;
        };
    }
}
