package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.dto.DepartmentDto;
import me.ronygomes.ums.api.exception.ErrorMessage;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.service.DepartmentService;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static me.ronygomes.ums.api.testHelper.DataHelper.mockDBDepartments;
import static me.ronygomes.ums.api.testHelper.DataHelper.validPersistableDepartment1;
import static me.ronygomes.ums.api.testHelper.RoleHelper.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("controller-test")
public class DepartmentControllerTest {

    private static final String JSON_DATE = """
            {
              "code": "CODE-1",
              "name": "Name 1"
            }
            """;

    @MockBean
    private DepartmentService departmentService;

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
    void testGetDepartments() throws Exception {
        Mockito.when(departmentService.findAll()).thenReturn(mockDBDepartments());

        mockMvc.perform(get("/v1/departments")
                        .accept("application/prs.hal-forms+json")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.length()").value(3))

                .andExpect(jsonPath("$._links.self.href").value("http://localhost/v1/departments"))

                .andExpect(jsonPath("$._templates.default.method").value("POST"))
                .andExpect(jsonPath("$._templates.default.properties.length()").value(2))
                .andExpect(jsonPath("$._templates.default.properties[0].name").value("code"))
                .andExpect(jsonPath("$._templates.default.properties[0].required").value("true"))
                .andExpect(jsonPath("$._templates.default.properties[0].min").value("1"))
                .andExpect(jsonPath("$._templates.default.properties[0].max").value("10"))
                .andExpect(jsonPath("$._templates.default.properties[0].type").value("range"))
                .andExpect(jsonPath("$._templates.default.properties[1].name").value("name"))
                .andExpect(jsonPath("$._templates.default.properties[1].required").value("true"))
                .andExpect(jsonPath("$._templates.default.properties[1].min").value("1"))
                .andExpect(jsonPath("$._templates.default.properties[1].max").value("100"))
                .andExpect(jsonPath("$._templates.default.properties[1].type").value("range"))

                .andExpect(jsonPath("$._embedded.departments.length()").value(2))
                .andExpect(jsonPath("$._embedded.departments[0].code").value("CODE-1"))
                .andExpect(jsonPath("$._embedded.departments[0].name").value("Name-1"))
                .andExpect(jsonPath("$._embedded.departments[0]._links.department.href").value("http://localhost/v1/departments/CODE-1"))
                .andExpect(jsonPath("$._embedded.departments[1].code").value("CODE-2"))
                .andExpect(jsonPath("$._embedded.departments[1].name").value("Name-2"))
                .andExpect(jsonPath("$._embedded.departments[1]._links.department.href").value("http://localhost/v1/departments/CODE-2"));

        mockMvc.perform(get("/v1/departments")
                        .accept("application/prs.hal-forms+json")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/departments")
                        .accept("application/prs.hal-forms+json")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testGetDepartment() throws Exception {
        DepartmentDto department = new DepartmentDto(validPersistableDepartment1());
        Mockito.when(departmentService.findByCode("CODE-1")).thenReturn(department);

        mockMvc.perform(get("/v1/departments/CODE-1")
                        .accept("application/prs.hal-forms+json")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$.code").value("CODE-1"))
                .andExpect(jsonPath("$.name").value("Name-1"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/v1/departments/CODE-1"))
                .andExpectAll(templateMatchers());

        mockMvc.perform(get("/v1/departments/CODE-1")
                        .accept("application/prs.hal-forms+json")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/departments/CODE-1")
                        .accept("application/prs.hal-forms+json")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testGetDepartmentNotFound() throws Exception {
        UmsDataException ex = new UmsDataException(ExceptionType.ENTITY_NOT_FOUND, "abc");
        Mockito.when(departmentService.findByCode("CODE-X")).thenThrow(ex);

        mockMvc.perform(get("/v1/departments/CODE-X")
                        .accept("application/prs.hal-forms+json")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$.type").value("https://documentation.com/errors/entity-not-found"))
                .andExpect(jsonPath("$.title").value("Requested object not found"))
                .andExpect(jsonPath("$.detail").value("abc"))
                .andExpect(jsonPath("$.instance").value("/v1/departments/CODE-X"));

        mockMvc.perform(get("/v1/departments/CODE-X")
                        .accept("application/prs.hal-forms+json")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/departments/CODE-X")
                        .accept("application/prs.hal-forms+json")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testPostDepartmentSuccess() throws Exception {

        ArgumentCaptor<DepartmentDto> ac = ArgumentCaptor.forClass(DepartmentDto.class);
        Mockito.doNothing().when(departmentService).save(ac.capture());

        DepartmentDto dto = new DepartmentDto();
        dto.setCode("CODE-DB");
        dto.setName("Name DB");

        Mockito.when(departmentService.findByCode("CODE-1")).thenReturn(dto);

        mockMvc.perform(post("/v1/departments")
                        .accept("application/prs.hal-forms+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/departments/CODE-1"))
                .andExpect(jsonPath("$").doesNotExist());

        DepartmentDto input = ac.getValue();
        Assertions.assertEquals("CODE-1", input.getCode());
        Assertions.assertEquals("Name 1", input.getName());

        mockMvc.perform(post("/v1/departments")
                        .accept("application/prs.hal-forms+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(post("/v1/departments")
                        .accept("application/prs.hal-forms+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testPostDepartmentError() throws Exception {

        ArgumentCaptor<DepartmentDto> ac = ArgumentCaptor.forClass(DepartmentDto.class);

        ErrorMessage em = new ErrorMessage("*", "Transaction Failed");
        UmsDataException ex = new UmsDataException(ExceptionType.DATA_VALIDATION_FAILED, "xyz", new ArrayList<>(List.of(em)));
        Mockito.doThrow(ex).when(departmentService).save(ac.capture());

        mockMvc.perform(post("/v1/departments")
                        .accept("application/prs.hal-forms+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.type").value("https://documentation.com/errors/data-validation-failed"))
                .andExpect(jsonPath("$.title").value("Provided data is not valid"))
                .andExpect(jsonPath("$.detail").value("xyz"))
                .andExpect(jsonPath("$.instance").value("/v1/departments"))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("*"))
                .andExpect(jsonPath("$.errors[0].message").value("Transaction Failed"));

        DepartmentDto input = ac.getValue();
        Assertions.assertEquals("CODE-1", input.getCode());
        Assertions.assertEquals("Name 1", input.getName());
    }

    @Test
    void testPutDepartmentSuccess() throws Exception {
        Mockito.when(departmentService.findByCode("CODE-1")).thenReturn(new DepartmentDto());

        ArgumentCaptor<DepartmentDto> ac = ArgumentCaptor.forClass(DepartmentDto.class);
        Mockito.doNothing().when(departmentService).updateAll(Mockito.eq("CODE-OLD"), ac.capture());

        mockMvc.perform(put("/v1/departments/CODE-OLD")
                        .accept("application/prs.hal-forms+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/departments/CODE-1"))
                .andExpect(jsonPath("$").doesNotExist());

        Assertions.assertEquals("CODE-1", ac.getValue().getCode());
        Assertions.assertEquals("Name 1", ac.getValue().getName());

        mockMvc.perform(put("/v1/departments/CODE-OLD")
                        .accept("application/prs.hal-forms+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(put("/v1/departments/CODE-OLD")
                        .accept("application/prs.hal-forms+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testPatchDepartmentSuccess() throws Exception {
        Mockito.when(departmentService.findByCode("CODE-1")).thenReturn(new DepartmentDto());

        ArgumentCaptor<DepartmentDto> ac = ArgumentCaptor.forClass(DepartmentDto.class);
        Mockito.doNothing().when(departmentService).updateOne(Mockito.eq("CODE-OLD"), ac.capture());

        mockMvc.perform(patch("/v1/departments/CODE-OLD")
                        .accept("application/prs.hal-forms+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/departments/CODE-1"))
                .andExpect(jsonPath("$").doesNotExist());

        Assertions.assertEquals("CODE-1", ac.getValue().getCode());
        Assertions.assertEquals("Name 1", ac.getValue().getName());

        mockMvc.perform(patch("/v1/departments/CODE-OLD")
                        .accept("application/prs.hal-forms+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(patch("/v1/departments/CODE-OLD")
                        .accept("application/prs.hal-forms+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testPatchDepartmentDelete() throws Exception {
        mockMvc.perform(delete("/v1/departments/CODE-X")
                        .accept("application/prs.hal-forms+json")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()));

        Mockito.verify(departmentService, Mockito.times(1)).delete("CODE-X");

        mockMvc.perform(delete("/v1/departments/CODE-X")
                .accept("application/prs.hal-forms+json")
                .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(delete("/v1/departments/CODE-X")
                        .accept("application/prs.hal-forms+json")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    private ResultMatcher[] templateMatchers() {
        return new ResultMatcher[]{
                jsonPath("$._templates.length()").value(2),
                jsonPath("$._templates.default.properties[0].name").value("code"),
                jsonPath("$._templates.default.properties[0].required").value("true"),
                jsonPath("$._templates.default.properties[0].min").value("1"),
                jsonPath("$._templates.default.properties[0].max").value("10"),
                jsonPath("$._templates.default.properties[0].type").value("range"),
                jsonPath("$._templates.default.properties[1].name").value("name"),
                jsonPath("$._templates.default.properties[1].required").value("true"),
                jsonPath("$._templates.default.properties[1].min").value("1"),
                jsonPath("$._templates.default.properties[1].max").value("100"),
                jsonPath("$._templates.default.properties[1].type").value("range"),

                jsonPath("$._templates.patchDepartment.method").value("PATCH"),
                jsonPath("$._templates.patchDepartment.properties.length()").value(2),
                jsonPath("$._templates.patchDepartment.properties[0].name").value("code"),
                jsonPath("$._templates.patchDepartment.properties[0].min").value("1"),
                jsonPath("$._templates.patchDepartment.properties[0].max").value("10"),
                jsonPath("$._templates.patchDepartment.properties[0].type").value("range"),
                jsonPath("$._templates.patchDepartment.properties[1].name").value("name"),
                jsonPath("$._templates.patchDepartment.properties[1].min").value("1"),
                jsonPath("$._templates.patchDepartment.properties[1].max").value("100"),
                jsonPath("$._templates.patchDepartment.properties[1].type").value("range")

        };
    }
}
