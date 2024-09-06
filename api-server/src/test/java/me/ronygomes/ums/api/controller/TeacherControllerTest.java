package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.assembler.TeacherModelHelper;
import me.ronygomes.ums.api.config.WebConfig;
import me.ronygomes.ums.api.dto.TeacherDto;
import me.ronygomes.ums.api.dto.TeacherPatchInputDto;
import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Designation;
import me.ronygomes.ums.api.model.Teacher;
import me.ronygomes.ums.api.service.TeacherService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherController.class)
@ContextConfiguration(classes = {WebConfig.class})
public class TeacherControllerTest {

    private static final String JSON_DATE = """
            {
              "fullName": "John Doe",
              "address": "Address 1",
              "email": "john@example.com",
              "contactNumber": "+1111111111111",
              "assignedCredit": 3.5,
              "title": "Title",
              "departmentCode": "CSE"
            }
            """;

    @MockBean
    private TeacherService teacherService;

    @SpyBean
    private TeacherModelHelper teacherModelAssembler;

    @Autowired
    private MappingJackson2HttpMessageConverter jackson2HttpMessageConverter;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TeacherController(teacherService, teacherModelAssembler))
                .setMessageConverters(jackson2HttpMessageConverter)
                .build();
    }

    @Test
    void testGetAllTeacherSuccess() throws Exception {
        Teacher mockDBTeacher = createMockDBTeacher();
        Mockito.when(teacherService.findAll()).thenReturn(List.of(mockDBTeacher));

        mockMvc.perform(get("/v1/teachers"))
                .andDo(print())
                .andExpect(jsonPath("$._embedded.teachers.length()").value("1"))
                .andExpect(jsonPath("$._embedded.teachers[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$._embedded.teachers[0].email").value("john@example.com"))
                .andExpect(jsonPath("$._embedded.teachers[0].title").value("Sample Title"))
                .andExpect(jsonPath("$._embedded.teachers[0].departmentCode").value("CODE-1"))
                .andExpect(jsonPath("$._embedded.teachers[0]._links.self.href")
                        .value("http://localhost/v1/teachers/3"))
                .andExpect(jsonPath("$._embedded.teachers[0]._links.department.href")
                        .value("http://localhost/v1/departments/CODE-1"))
                .andExpect(jsonPath("$._embedded.teachers[0]._links.designation.href")
                        .value("http://localhost/v1/designations/1"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/v1/teachers"))
                .andExpect(status().is(HttpStatus.OK.value()));

        Mockito.verify(teacherModelAssembler, Mockito.times(1)).toCollectionModel(Mockito.any());
    }

    @Test
    void testGetTeacherSuccess() throws Exception {
        Teacher mockDBTeacher = createMockDBTeacher();
        Mockito.when(teacherService.findById(mockDBTeacher.getId())).thenReturn(mockDBTeacher);

        mockMvc.perform(get("/v1/teachers/" + mockDBTeacher.getId())
                        // Didn't implement HAL forms
                        .accept("application/hal+json"))
                .andDo(print())
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.address").value("Somewhere 1"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.contactNumber").value("+5501349287652"))
                .andExpect(jsonPath("$.title").value("Sample Title"))
                .andExpect(jsonPath("$.departmentCode").value("CODE-1"))
                .andExpect(jsonPath("$.assignedCredit").value("10.0"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/v1/teachers/3"))

                .andExpect(jsonPath("$._embedded.designation.title").value("Sample Title"))
                .andExpect(jsonPath("$._embedded.designation._links.self.href").value("http://localhost/v1/designations/1"))

                .andExpect(jsonPath("$._embedded.department.code").value("CODE-1"))
                .andExpect(jsonPath("$._embedded.department.name").value("Name-1"))
                .andExpect(jsonPath("$._embedded.department._links.self.href").value("http://localhost/v1/departments/CODE-1"))

                .andExpect(status().is(HttpStatus.OK.value()));

        Mockito.verify(teacherModelAssembler, Mockito.times(1)).toModel(Mockito.any());
    }

    @Test
    void testPostTeacherSuccess() throws Exception {

        ArgumentCaptor<TeacherDto> ac = ArgumentCaptor.forClass(TeacherDto.class);
        Mockito.when(teacherService.create(ac.capture())).thenReturn(500L);

        mockMvc.perform(post("/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATE))
                .andDo(print())
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/teachers/500"))
                .andExpect(jsonPath("$").doesNotExist());

        TeacherDto received = ac.getValue();
        Assertions.assertEquals("John Doe", received.getFullName());
        Assertions.assertEquals("Address 1", received.getAddress());
        Assertions.assertEquals("john@example.com", received.getEmail());
        Assertions.assertEquals("+1111111111111", received.getContactNumber());
        Assertions.assertEquals(3.5, received.getAssignedCredit());
        Assertions.assertEquals("Title", received.getTitle());
        Assertions.assertEquals("CSE", received.getDepartmentCode());

        Mockito.verifyNoInteractions(teacherModelAssembler);
    }

    @Test
    void testUpdateTeacherSuccess() throws Exception {
        Teacher mockDBTeacher = createMockDBTeacher();

        String updateJson = """
                {
                  "fullName": "Jane Doe",
                  "address": "Address Updated",
                  "email": "updated@example.com",
                  "contactNumber": "+2111111111111",
                  "assignedCredit": 4.5,
                  "title": "Title 2",
                  "departmentCode": "AC"
                }
                """;

        ArgumentCaptor<TeacherDto> ac = ArgumentCaptor.forClass(TeacherDto.class);
        Mockito.doNothing().when(teacherService).updateAll(Mockito.eq(mockDBTeacher.getId()), ac.capture());

        mockMvc.perform(put("/v1/teachers/" + mockDBTeacher.getId())
                        .contentType("application/json")
                        .content(updateJson))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/teachers/" + mockDBTeacher.getId()))
                .andExpect(status().is(HttpStatus.ACCEPTED.value()));

        TeacherDto received = ac.getValue();
        Assertions.assertEquals("Jane Doe", received.getFullName());
        Assertions.assertEquals("Address Updated", received.getAddress());
        Assertions.assertEquals("updated@example.com", received.getEmail());
        Assertions.assertEquals("+2111111111111", received.getContactNumber());
        Assertions.assertEquals(4.5, received.getAssignedCredit());
        Assertions.assertEquals("Title 2", received.getTitle());
        Assertions.assertEquals("AC", received.getDepartmentCode());
    }

    @Test
    void testUpdatePatchTeacherSuccess() throws Exception {
        Teacher mockDBTeacher = createMockDBTeacher();

        String updateJson = """
                {
                  "address": "Address Updated",
                  "assignedCredit": 4.5
                }
                """;

        ArgumentCaptor<TeacherPatchInputDto> ac = ArgumentCaptor.forClass(TeacherPatchInputDto.class);
        Mockito.doNothing().when(teacherService).updateProvided(Mockito.eq(mockDBTeacher.getId()), ac.capture());

        mockMvc.perform(patch("/v1/teachers/" + mockDBTeacher.getId())
                        .contentType("application/json")
                        .content(updateJson))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/teachers/" + mockDBTeacher.getId()))
                .andExpect(status().is(HttpStatus.ACCEPTED.value()));

        TeacherPatchInputDto received = ac.getValue();
        Assertions.assertNull(received.getFullName());
        Assertions.assertEquals("Address Updated", received.getAddress());
        Assertions.assertNull(received.getEmail());
        Assertions.assertNull(received.getContactNumber());
        Assertions.assertEquals(0, received.getAssignedCredit().compareTo(4.5f));
        Assertions.assertNull(received.getTitle());
        Assertions.assertNull(received.getDepartmentCode());
    }

    @Test
    void testDeleteTeacherSuccess() throws Exception {
        Teacher mockDBTeacher = createMockDBTeacher();

        mockMvc.perform(delete("/v1/teachers/" + mockDBTeacher.getId()))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().is(HttpStatus.ACCEPTED.value()));

        Mockito.verify(teacherService, Mockito.times(1)).delete(3L);
    }

    private Teacher createMockDBTeacher() {
        Designation designation = DataHelper.validPersistableDesignation();
        designation.setId(1L);

        Department department = DataHelper.validPersistableDepartment1();
        department.setId(2L);

        Teacher dbTeacher = DataHelper.validPersistableTeacher1(designation, department);
        dbTeacher.setId(3L);

        return dbTeacher;
    }
}