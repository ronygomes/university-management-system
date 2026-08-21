package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.dto.CourseDto;
import me.ronygomes.ums.api.dto.CoursePatchDto;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Semester;
import me.ronygomes.ums.api.model.Teacher;
import me.ronygomes.ums.api.service.CourseService;
import me.ronygomes.ums.api.testHelper.DataHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static me.ronygomes.ums.api.testHelper.RoleHelper.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("controller-test")
public class CourseControllerTest {

    private static final String JSON_DATE = """
            {
              "code": "Title",
              "name": "Course Name",
              "credit": 3.5,
              "description": "Awesome",
              "semester": "FOURTH_YEAR_SECOND",
              "departmentCode": "CSE",
              "instructorIds": [30]
            }
            """;

    @MockitoBean
    private CourseService courseService;

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
    void testFindPagedSuccess() throws Exception {
        CourseDto dto1 = CourseDto.toDto(createMockDBCourse());
        dto1.setId(1L);
        CourseDto dto2 = CourseDto.toDto(createMockDBCourse());
        dto2.setId(2L);
        dto2.setCode("CSE-102");

        Mockito.when(courseService.findPaged(Mockito.any(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 20), 2));

        mockMvc.perform(get("/v1/courses")
                        .param("departmentCode", "CSE")
                        .param("semester", "FIRST_YEAR_FIRST")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].code").value("CSE-101"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].code").value("CSE-102"));

        Mockito.verify(courseService, Mockito.times(1))
                .findPaged(Mockito.eq("CSE"), Mockito.eq(Semester.FIRST_YEAR_FIRST), Mockito.any(Pageable.class));

        mockMvc.perform(get("/v1/courses").with(teacherJwt()))
                .andExpect(status().is(HttpStatus.OK.value()));

        mockMvc.perform(get("/v1/courses").with(studentJwt()))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void testFindByIdSuccess() throws Exception {
        CourseDto courseDto = CourseDto.toDto(createMockDBCourse());
        Mockito.when(courseService.findById(1L)).thenReturn(courseDto);

        mockMvc.perform(get("/v1/courses/1")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$.code").value("CSE-101"))
                .andExpect(jsonPath("$.name").value("Introduction to Programming Language in Java"))
                .andExpect(jsonPath("$.credit").value("3.0"))
                .andExpect(jsonPath("$.description").value("Java Description"))
                .andExpect(jsonPath("$.departmentCode").value("CODE-1"))
                .andExpect(jsonPath("$.semester").value("FIRST_YEAR_FIRST"))
                .andExpect(jsonPath("$.instructorIds[0]").value(3))
                .andExpect(status().is(HttpStatus.OK.value()));

        mockMvc.perform(get("/v1/courses/1")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/courses/1")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testFindByIdFailure() throws Exception {
        UmsDataException ex = new UmsDataException(ExceptionType.ENTITY_NOT_FOUND, "abc");
        Mockito.when(courseService.findById(1L)).thenThrow(ex);

        mockMvc.perform(get("/v1/courses/1")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$.length()").value("4"))
                .andExpect(jsonPath("$.type").value("https://documentation.com/errors/entity-not-found"))
                .andExpect(jsonPath("$.title").value("Requested object not found"))
                .andExpect(jsonPath("$.detail").value("abc"))
                .andExpect(jsonPath("$.instance").value("/v1/courses/1"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void testPostSuccess() throws Exception {
        ArgumentCaptor<CourseDto> ac = ArgumentCaptor.forClass(CourseDto.class);
        Mockito.when(courseService.create(ac.capture())).thenReturn(500L);

        mockMvc.perform(post("/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/courses/500"))
                .andExpect(jsonPath("$").doesNotExist());

        CourseDto received = ac.getValue();
        Assertions.assertEquals("Title", received.getCode());
        Assertions.assertEquals("Course Name", received.getName());
        Assertions.assertEquals(BigDecimal.valueOf(3.5), received.getCredit());
        Assertions.assertEquals("Awesome", received.getDescription());
        Assertions.assertEquals(Semester.FOURTH_YEAR_SECOND, received.getSemester());
        Assertions.assertEquals("CSE", received.getDepartmentCode());
        Assertions.assertEquals(List.of(30L), received.getInstructorIds());

        mockMvc.perform(post("/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(post("/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testPutSuccess() throws Exception {
        String updateJson = """
                {
                  "code": "Title 1",
                  "name": "Course Name 2",
                  "credit": 3.3,
                  "description": "Awesome 4",
                  "semester": "FOURTH_YEAR_FIRST",
                  "departmentCode": "EEE",
                  "instructorIds": [31]
                }
                """;

        ArgumentCaptor<CourseDto> ac = ArgumentCaptor.forClass(CourseDto.class);
        Mockito.doNothing().when(courseService).updateAll(Mockito.eq(5L), ac.capture());

        mockMvc.perform(put("/v1/courses/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/courses/5"))
                .andExpect(jsonPath("$").doesNotExist());

        CourseDto received = ac.getValue();
        Assertions.assertEquals("Title 1", received.getCode());
        Assertions.assertEquals("Course Name 2", received.getName());
        Assertions.assertEquals(BigDecimal.valueOf(3.3), received.getCredit());
        Assertions.assertEquals("Awesome 4", received.getDescription());
        Assertions.assertEquals(Semester.FOURTH_YEAR_FIRST, received.getSemester());
        Assertions.assertEquals("EEE", received.getDepartmentCode());
        Assertions.assertEquals(List.of(31L), received.getInstructorIds());

        mockMvc.perform(put("/v1/courses/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(put("/v1/courses/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testPatchSuccess() throws Exception {
        String updateJson = """
                {
                  "code": "Title 1",
                  "departmentCode": "EEE"
                }
                """;

        ArgumentCaptor<CoursePatchDto> ac = ArgumentCaptor.forClass(CoursePatchDto.class);
        Mockito.doNothing().when(courseService).updateProvided(Mockito.eq(5L), ac.capture());

        mockMvc.perform(patch("/v1/courses/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/courses/5"))
                .andExpect(jsonPath("$").doesNotExist());

        CoursePatchDto received = ac.getValue();
        Assertions.assertEquals("Title 1", received.getCode());
        Assertions.assertNull(received.getName());
        Assertions.assertNull(received.getCredit());
        Assertions.assertNull(received.getDescription());
        Assertions.assertNull(received.getSemester());
        Assertions.assertEquals("EEE", received.getDepartmentCode());
        Assertions.assertNull(received.getInstructorIds());

        mockMvc.perform(patch("/v1/courses/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(patch("/v1/courses/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testDeleteSuccess() throws Exception {
        mockMvc.perform(delete("/v1/courses/1")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()));

        Mockito.verify(courseService, Mockito.times(1)).delete(1L);

        mockMvc.perform(delete("/v1/courses/1")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(delete("/v1/courses/1")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    private Course createMockDBCourse() {
        Department d = DataHelper.validPersistableDepartment1();
        d.setId(2L);

        Teacher t = new Teacher();
        t.setId(3L);

        Course c = DataHelper.validPersistableCourse1(d, t);
        c.setId(1L);

        return c;
    }
}
