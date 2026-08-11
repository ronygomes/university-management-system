package me.ronygomes.ums.api.handler;

import jakarta.servlet.http.HttpServletRequest;
import me.ronygomes.ums.api.exception.ErrorMessage;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private static final String UMS_SERVICE_EXCEPTION_METHOD_NAME = "handleUmsServiceException";

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHasUmsServiceException() throws NoSuchMethodException {
        Method method = GlobalExceptionHandler.class.getDeclaredMethod(UMS_SERVICE_EXCEPTION_METHOD_NAME,
                UmsDataException.class, HttpServletRequest.class);

        Assertions.assertNotNull(method);
        Assertions.assertEquals(1, method.getAnnotations().length);

        ExceptionHandler anonEx = method.getAnnotation(ExceptionHandler.class);
        Assertions.assertEquals(1, anonEx.value().length);
        Assertions.assertEquals(UmsDataException.class, anonEx.value()[0]);
    }

    @Test
    void testEntityNotFoundException() {
        UmsDataException ex = new UmsDataException(ExceptionType.ENTITY_NOT_FOUND, "xyz");

        Mockito.when(request.getRequestURI()).thenReturn("v1/dummy/456");

        ResponseEntity<Problem> response = handler.handleUmsServiceException(ex, request);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Problem.ExtendedProblem<Map<String, Object>> p = (Problem.ExtendedProblem<Map<String, Object>>) response.getBody();

        Assertions.assertEquals("https://documentation.com/errors/entity-not-found", p.getType().toASCIIString());
        Assertions.assertEquals("Requested object not found", p.getTitle());
        Assertions.assertEquals("xyz", p.getDetail());
        Assertions.assertEquals("v1/dummy/456", p.getInstance().toASCIIString());

        Assertions.assertEquals(0, p.getProperties().size());
    }

    @Test
    void testDataValidationException() {
        ErrorMessage em = new ErrorMessage("text", "Length is less than 100");
        UmsDataException ex = new UmsDataException(ExceptionType.DATA_VALIDATION_FAILED, "abc",
                new ArrayList<>(List.of(em)));

        Mockito.when(request.getRequestURI()).thenReturn("v1/dummy/123");

        ResponseEntity<Problem> response = handler.handleUmsServiceException(ex, request);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Problem.ExtendedProblem<Map<String, Object>> p = (Problem.ExtendedProblem<Map<String, Object>>) response.getBody();

        Assertions.assertEquals("https://documentation.com/errors/data-validation-failed", p.getType().toASCIIString());
        Assertions.assertEquals("Provided data is not valid", p.getTitle());
        Assertions.assertEquals("abc", p.getDetail());
        Assertions.assertEquals("v1/dummy/123", p.getInstance().toASCIIString());

        Map<String, Object> prop = p.getProperties();
        Assertions.assertEquals(1, prop.size());

        List<ErrorMessage> errors = (List<ErrorMessage>) prop.get("errors");
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("text", errors.get(0).getField());
        Assertions.assertEquals("Length is less than 100", errors.get(0).getMessage());
    }
}
