package me.ronygomes.ums.api.handler;

import jakarta.servlet.http.HttpServletRequest;
import me.ronygomes.ums.api.exception.UmsDataException;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UmsDataException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Problem handleUmsServiceException(UmsDataException ex, HttpServletRequest request) {
        return Problem.create()
                .withType(ex.getExceptionType().getDocumentationUrl())
                .withTitle(ex.getExceptionType().getTitle())
                .withDetail(ex.getErrorDetails())
                .withInstance(URI.create(request.getRequestURI()))
                .withProperties(m -> {
                    if (!ex.getErrors().isEmpty()) {
                        m.put("errors", ex.getErrors());
                    }
                });
    }
}
