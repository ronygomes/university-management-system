package me.ronygomes.ums.api.controller;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Building;
import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.CourseSchedule;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.repository.CourseRepository;
import me.ronygomes.ums.api.repository.CourseScheduleRepository;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import me.ronygomes.ums.api.testHelper.DataHelper;
import me.ronygomes.ums.api.validator.CourseScheduleValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static me.ronygomes.ums.api.model.Semester.FOURTH_YEAR_SECOND;
import static me.ronygomes.ums.api.testHelper.RoleHelper.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("controller-test")
public class CourseScheduleControllerTest {

    // Note: @JsonIgnore disallows read+write but @JsonProperty(READ) will allow only read
    private static final String JSON_DATE = """
            {
              "id": "3",
              "course": {
                "id": 100
              },
              "department": {
                "code": "CSE"
              },
              "courseId": "100",
              "departmentCode": "CSE",
              "semester": "FOURTH_YEAR_SECOND",
              "building": "BUILDING_1",
              "roomNumber": "410-B",
              "days": ["FRIDAY", "MONDAY"],
              "startDate": "2024-09-03",
              "endDate": "2024-10-03"
            }
            """;

    @MockitoBean
    private CourseScheduleRepository courseScheduleRepository;

    @MockitoBean
    private DepartmentRepository departmentRepository;

    @MockitoBean
    private CourseRepository courseRepository;

    @MockitoBean
    private ExceptionHelper exceptionHelper;

    @MockitoBean
    private CourseScheduleValidator courseScheduleValidator;

    @MockitoBean
    private Validator validator;

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
        CourseSchedule cs1 = mockCourseSchedule();
        CourseSchedule cs2 = mockCourseSchedule();
        cs2.setId(99L);
        cs2.setRoomNumber("F7-203");

        Mockito.when(courseScheduleRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cs1, cs2), PageRequest.of(0, 20), 2));

        mockMvc.perform(get("/v1/schedules")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].roomNumber").value("F7-102"))
                .andExpect(jsonPath("$.content[1].id").value(99))
                .andExpect(jsonPath("$.content[1].roomNumber").value("F7-203"));

        mockMvc.perform(get("/v1/schedules")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.OK.value()));

        mockMvc.perform(get("/v1/schedules")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void testFindByIdSuccess() throws Exception {
        CourseSchedule cs = mockCourseSchedule();
        Mockito.when(courseScheduleRepository.findById(1L)).thenReturn(Optional.of(cs));

        mockMvc.perform(get("/v1/schedules/1")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$.id").value("3"))
                .andExpect(jsonPath("$.courseId").value("2"))
                .andExpect(jsonPath("$.department.code").value("CODE-1"))
                .andExpect(jsonPath("$.department.name").value("Name-1"))
                .andExpect(jsonPath("$.semester").value("FIRST_YEAR_SECOND"))
                .andExpect(jsonPath("$.building").value("BUILDING_1"))
                .andExpect(jsonPath("$.roomNumber").value("F7-102"))
                .andExpect(jsonPath("$.days").isArray())
                .andExpect(jsonPath("$.days[0]").value("MONDAY"))
                .andExpect(jsonPath("$.days[1]").value("TUESDAY"))
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists())
                .andExpect(jsonPath("$.course").doesNotExist())
                .andExpect(status().is(HttpStatus.OK.value()));

        mockMvc.perform(get("/v1/schedules/1")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/schedules/1")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testFindByCourseIdSuccess() throws Exception {
        CourseSchedule cs1 = mockCourseSchedule();
        CourseSchedule cs2 = mockCourseSchedule();
        cs2.setId(4L);

        Mockito.when(courseScheduleRepository.findByCourseId(1L))
                .thenReturn(Arrays.asList(cs1, cs2));

        mockMvc.perform(get("/v1/schedules/course/1")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[1].id").value(4))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void testCreteSuccess() throws Exception {

        Mockito.when(courseScheduleValidator.supports(Mockito.any())).thenReturn(true);
        Mockito.doNothing().when(exceptionHelper).throwErrorIfValidationError(Mockito.any(), Mockito.any(), Mockito.any());

        Department d = new Department();
        Mockito.when(departmentRepository.findByCode("CSE")).thenReturn(Optional.of(d));

        Course c = new Course();
        Mockito.when(courseRepository.findById(100L)).thenReturn(Optional.of(c));

        ArgumentCaptor<CourseSchedule> ac = ArgumentCaptor.forClass(CourseSchedule.class);
        Mockito.doAnswer(i -> {
            CourseSchedule sc = i.getArgument(0);

            Assertions.assertNull(sc.getId());
            sc.setId(100L);

            return null;
        }).when(courseScheduleRepository).save(ac.capture());

        mockMvc.perform(post("/v1/schedules")
                        .contentType("application/json")
                        .content(JSON_DATE)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(header().string("Location", "http://localhost/v1/schedules/100"))
                .andExpect(status().is(HttpStatus.CREATED.value()));

        CourseSchedule sc = ac.getValue();

        // Disallowed fields
        Assertions.assertSame(d, sc.getDepartment());
        Assertions.assertSame(c, sc.getCourse());

        Assertions.assertEquals("CSE", sc.getDepartmentCode());
        Assertions.assertEquals(FOURTH_YEAR_SECOND, sc.getSemester());
        Assertions.assertEquals(100L, sc.getCourseId());
        Assertions.assertEquals(Building.BUILDING_1, sc.getBuilding());
        Assertions.assertEquals("410-B", sc.getRoomNumber());
        Assertions.assertIterableEquals(List.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY), sc.getDays());

        Assertions.assertEquals(LocalDate.of(2024, 9, 3), sc.getStartDate());
        Assertions.assertEquals(LocalDate.of(2024, 10, 3), sc.getEndDate());

        mockMvc.perform(post("/v1/schedules")
                        .contentType("application/json")
                        .content(JSON_DATE)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(post("/v1/schedules")
                        .contentType("application/json")
                        .content(JSON_DATE)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testFindByIdFailed() throws Exception {
        Mockito.when(courseScheduleRepository.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/v1/schedules/1")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$.type").value("https://documentation.com/errors/entity-not-found"))
                .andExpect(jsonPath("$.title").value("Requested object not found"))
                .andExpect(jsonPath("$.detail").value("Course Schedule with id '1' not found"))
                .andExpect(jsonPath("$.instance").value("/v1/schedules/1"))
                .andExpect(jsonPath("$.length()").value("4"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void testUpdateSuccess() throws Exception {

        Mockito.when(courseScheduleValidator.supports(Mockito.any())).thenReturn(true);
        Mockito.doNothing().when(exceptionHelper).throwErrorIfValidationError(Mockito.any(), Mockito.any(), Mockito.any());

        Department d = new Department();
        Mockito.when(departmentRepository.findByCode("CSE")).thenReturn(Optional.of(d));

        Course c = new Course();
        Mockito.when(courseRepository.findById(100L)).thenReturn(Optional.of(c));

        CourseSchedule cs = new CourseSchedule();
        Mockito.when(courseScheduleRepository.findById(1L)).thenReturn(Optional.of(cs));

        ArgumentCaptor<CourseSchedule> ac = ArgumentCaptor.forClass(CourseSchedule.class);
        Mockito.when(courseScheduleRepository.save(ac.capture())).thenReturn(null);

        mockMvc.perform(put("/v1/schedules/1")
                        .contentType("application/json")
                        .content(JSON_DATE)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(header().string("Location", "http://localhost/v1/schedules/1"))
                .andExpect(status().is(HttpStatus.ACCEPTED.value()));

        CourseSchedule sc = ac.getValue();

        // Disallowed fields
        Assertions.assertSame(d, sc.getDepartment());
        Assertions.assertSame(c, sc.getCourse());

        Assertions.assertEquals("CSE", sc.getDepartmentCode());
        Assertions.assertEquals(FOURTH_YEAR_SECOND, sc.getSemester());
        Assertions.assertEquals(100L, sc.getCourseId());
        Assertions.assertEquals(Building.BUILDING_1, sc.getBuilding());
        Assertions.assertEquals("410-B", sc.getRoomNumber());
        Assertions.assertIterableEquals(List.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY), sc.getDays());

        Assertions.assertEquals(LocalDate.of(2024, 9, 3), sc.getStartDate());
        Assertions.assertEquals(LocalDate.of(2024, 10, 3), sc.getEndDate());

        mockMvc.perform(put("/v1/schedules/1")
                        .contentType("application/json")
                        .content(JSON_DATE)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(put("/v1/schedules/1")
                        .contentType("application/json")
                        .content(JSON_DATE)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testUpdatePatchSuccess() throws Exception {

        Mockito.when(courseScheduleValidator.supports(Mockito.any())).thenReturn(true);
        Mockito.doNothing().when(exceptionHelper).throwErrorIfValidationError(Mockito.any(), Mockito.any(), Mockito.any());

        CourseSchedule cs = Mockito.mock(CourseSchedule.class);
        Mockito.when(courseScheduleRepository.findById(1L)).thenReturn(Optional.of(cs));

        Course c = new Course();
        c.setId(1L);
        Mockito.when(cs.getCourse()).thenReturn(c);

        Department d = new Department();
        Mockito.when(cs.getDepartment()).thenReturn(d);
        Mockito.when(departmentRepository.findByCode(Mockito.any())).thenReturn(Optional.of(d));
        Mockito.when(courseRepository.findById(Mockito.any())).thenReturn(Optional.of(c));

        String jsonData = """
                {
                  "roomNumber": "R123",
                  "departmentCode": "CSE"
                }
                """;

        mockMvc.perform(patch("/v1/schedules/1")
                        .contentType("application/json")
                        .content(jsonData)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(header().string("Location", "http://localhost/v1/schedules/1"))
                .andExpect(status().is(HttpStatus.ACCEPTED.value()));

        Mockito.verify(cs, Mockito.times(1)).merge(Mockito.any());
        Mockito.verify(courseScheduleRepository, Mockito.times(1)).save(cs);

        mockMvc.perform(patch("/v1/schedules/1")
                        .contentType("application/json")
                        .content(jsonData)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(patch("/v1/schedules/1")
                        .contentType("application/json")
                        .content(jsonData)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testDeleteSuccess() throws Exception {
        CourseSchedule cs = new CourseSchedule();
        Mockito.when(courseScheduleRepository.findById(1L)).thenReturn(Optional.of(cs));

        mockMvc.perform(delete("/v1/schedules/1")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()));

        Mockito.verify(courseScheduleRepository, Mockito.times(1)).delete(cs);

        mockMvc.perform(delete("/v1/schedules/1")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(delete("/v1/schedules/1")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testSetEnrollmentOpenSuccess() throws Exception {
        CourseSchedule cs = new CourseSchedule();
        cs.setEnrollmentOpen(false);
        Mockito.when(courseScheduleRepository.findById(7L)).thenReturn(Optional.of(cs));

        ArgumentCaptor<CourseSchedule> ac = ArgumentCaptor.forClass(CourseSchedule.class);
        Mockito.when(courseScheduleRepository.save(ac.capture())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(put("/v1/schedules/7/enrollment-open")
                        .param("open", "true")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/schedules/7"));

        Assertions.assertTrue(ac.getValue().isEnrollmentOpen());

        mockMvc.perform(put("/v1/schedules/7/enrollment-open")
                        .param("open", "false")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(put("/v1/schedules/7/enrollment-open")
                        .param("open", "false")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    private CourseSchedule mockCourseSchedule() {
        Department d = DataHelper.validPersistableDepartment1();
        d.setId(1L);

        Course c = DataHelper.validPersistableCourse1(d, null);
        c.setId(2L);

        CourseSchedule cs = DataHelper.validPersistableCourseSchedule1(d, c);
        cs.setId(3L);
        return cs;
    }
}
