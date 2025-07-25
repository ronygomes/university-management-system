package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.dto.EnrollmentDto;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.EnrollmentStatus;
import me.ronygomes.ums.api.model.Grade;
import me.ronygomes.ums.api.service.EnrollmentService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static me.ronygomes.ums.api.testHelper.RoleHelper.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("controller-test")
public class EnrollmentControllerTest {

    private static final String JSON_DATE = """
            {
              "id": 47,
              "courseScheduleId": 48,
              "studentId": 49,
              "grade": "A",
              "status": "PASSED",
              "enrollmentDate": "2024-09-04T17:03:43.849+00:00"
            }
            """;

    @MockBean
    private EnrollmentService enrollmentService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testFindByIdSuccess() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date d = new Date();

        EnrollmentDto e = createMockDBEnrollment(d);
        Mockito.when(enrollmentService.findById(1L)).thenReturn(e);

        mockMvc.perform(get("/v1/enrollments/1")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.enrollmentDate").value(Matchers.startsWith(formatter.format(d))))
                .andExpect(jsonPath("$.courseScheduleId").value("2"))
                .andExpect(jsonPath("$.studentId").value("3"))
                .andExpect(jsonPath("$.grade").value("F"))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.length()").value("6"))
                .andExpect(status().is(HttpStatus.OK.value()));

        mockMvc.perform(get("/v1/enrollments/1")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/enrollments/1")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testFindByIdFailure() throws Exception {
        UmsDataException ex = new UmsDataException(ExceptionType.ENTITY_NOT_FOUND, "abc");
        Mockito.when(enrollmentService.findById(1L)).thenThrow(ex);

        mockMvc.perform(get("/v1/enrollments/1")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$.length()").value("4"))
                .andExpect(jsonPath("$.type").value("https://documentation.com/errors/entity-not-found"))
                .andExpect(jsonPath("$.title").value("Requested object not found"))
                .andExpect(jsonPath("$.detail").value("abc"))
                .andExpect(jsonPath("$.instance").value("/v1/enrollments/1"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testPostSuccess() throws Exception {
        ArgumentCaptor<EnrollmentDto> ac = ArgumentCaptor.forClass(EnrollmentDto.class);
        Mockito.when(enrollmentService.create(ac.capture())).thenReturn(501L);

        mockMvc.perform(post("/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/enrollments/501"))
                .andExpect(jsonPath("$").doesNotExist());

        EnrollmentDto received = ac.getValue();
        Assertions.assertNull(received.getId()); // Can't bind id
        Assertions.assertEquals(48, received.getCourseScheduleId());
        Assertions.assertEquals(49, received.getStudentId());
        Assertions.assertEquals(Grade.A, received.getGrade());
        Assertions.assertEquals(EnrollmentStatus.PASSED, received.getStatus());
        Assertions.assertTrue(received.getEnrollmentDate().before(new Date()));

        mockMvc.perform(post("/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(post("/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testUpdateSuccess() throws Exception {
        ArgumentCaptor<EnrollmentDto> ac = ArgumentCaptor.forClass(EnrollmentDto.class);
        Mockito.doNothing().when(enrollmentService).update(Mockito.eq(502L), ac.capture());

        mockMvc.perform(put("/v1/enrollments/502")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/enrollments/502"))
                .andExpect(jsonPath("$").doesNotExist());

        EnrollmentDto received = ac.getValue();
        Assertions.assertNull(received.getId()); // Can't bind id
        Assertions.assertEquals(48, received.getCourseScheduleId());
        Assertions.assertEquals(49, received.getStudentId());
        Assertions.assertEquals(Grade.A, received.getGrade());
        Assertions.assertEquals(EnrollmentStatus.PASSED, received.getStatus());
        Assertions.assertTrue(received.getEnrollmentDate().before(new Date()));

        mockMvc.perform(put("/v1/enrollments/502")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(put("/v1/enrollments/502")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testUpdateProvidedSuccess() throws Exception {
        ArgumentCaptor<EnrollmentDto> ac = ArgumentCaptor.forClass(EnrollmentDto.class);
        Mockito.doNothing().when(enrollmentService).updateProvided(Mockito.eq(503L), ac.capture());

        String jsonData = """
                {
                     "id": 47,
                     "courseScheduleId": 48,
                     "grade": "A"
                 }
                """;

        mockMvc.perform(patch("/v1/enrollments/503")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonData)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/enrollments/503"))
                .andExpect(jsonPath("$").doesNotExist());

        EnrollmentDto received = ac.getValue();
        Assertions.assertNull(received.getId()); // Can't bind id
        Assertions.assertEquals(48, received.getCourseScheduleId());
        Assertions.assertNull(received.getStudentId());
        Assertions.assertEquals(Grade.A, received.getGrade());
        Assertions.assertNull(received.getStatus());
        Assertions.assertNull(received.getEnrollmentDate());

        mockMvc.perform(patch("/v1/enrollments/503")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonData)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(patch("/v1/enrollments/503")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonData)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testDeleteSuccess() throws Exception {
        mockMvc.perform(delete("/v1/enrollments/5")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()));

        Mockito.verify(enrollmentService, Mockito.times(1)).delete(5L);

        mockMvc.perform(delete("/v1/enrollments/5")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(delete("/v1/enrollments/5")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    private EnrollmentDto createMockDBEnrollment(Date date) {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setId(1L);
        dto.setEnrollmentDate(date);
        dto.setCourseScheduleId(2L);
        dto.setStudentId(3L);
        dto.setGrade(Grade.F);
        dto.setStatus(EnrollmentStatus.FAILED);

        return dto;
    }
}
