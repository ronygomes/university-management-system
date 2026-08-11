package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.dto.EnrollmentDto;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Student;
import me.ronygomes.ums.api.model.Teacher;
import me.ronygomes.ums.api.service.EnrollmentService;
import me.ronygomes.ums.api.service.StudentService;
import me.ronygomes.ums.api.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static me.ronygomes.ums.api.testHelper.RoleHelper.adminJwt;
import static me.ronygomes.ums.api.testHelper.RoleHelper.studentJwt;
import static me.ronygomes.ums.api.testHelper.RoleHelper.teacherJwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("controller-test")
public class MeControllerTest {

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private TeacherService teacherService;

    @MockitoBean
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

    private static org.springframework.test.web.servlet.request.RequestPostProcessor studentJwtWithEmail(String email) {
        return jwt().jwt(j -> j.claim("email", email))
                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor teacherJwtWithEmail(String email) {
        return jwt().jwt(j -> j.claim("email", email))
                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TEACHER"));
    }

    @Test
    void testGetMyStudentSuccess() throws Exception {
        Student s = new Student();
        s.setId(42L);
        s.setFullName("Jane Doe");
        s.setEmail("jane@ums.dev");
        Department d = new Department();
        d.setCode("CSE");
        d.setName("CSE");
        s.setDepartment(d);

        Mockito.when(studentService.findByEmail("jane@ums.dev")).thenReturn(Optional.of(s));

        mockMvc.perform(get("/v1/me/student").with(studentJwtWithEmail("jane@ums.dev")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@ums.dev"));
    }

    @Test
    void testGetMyStudentForbiddenForNonStudent() throws Exception {
        mockMvc.perform(get("/v1/me/student").with(adminJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/me/student").with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testGetMyTeacherSuccess() throws Exception {
        Teacher t = new Teacher();
        t.setId(7L);
        t.setFullName("John Smith");
        t.setEmail("john@ums.dev");

        Mockito.when(teacherService.findByEmail("john@ums.dev")).thenReturn(Optional.of(t));

        mockMvc.perform(get("/v1/me/teacher").with(teacherJwtWithEmail("john@ums.dev")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.fullName").value("John Smith"))
                .andExpect(jsonPath("$.email").value("john@ums.dev"));
    }

    @Test
    void testGetMyTeacherForbiddenForNonTeacher() throws Exception {
        mockMvc.perform(get("/v1/me/teacher").with(adminJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/me/teacher").with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testGetMyEnrollmentsReturnsOnlyCurrentStudents() throws Exception {
        Student s = new Student();
        s.setId(42L);
        s.setEmail("jane@ums.dev");
        Mockito.when(studentService.findByEmail("jane@ums.dev")).thenReturn(Optional.of(s));

        EnrollmentDto e1 = new EnrollmentDto();
        e1.setId(100L);
        e1.setStudentId(42L);
        e1.setCourseScheduleId(200L);
        Mockito.when(enrollmentService.findByStudentId(42L)).thenReturn(List.of(e1));

        mockMvc.perform(get("/v1/me/enrollments").with(studentJwtWithEmail("jane@ums.dev")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].studentId").value(42));

        Mockito.verify(enrollmentService, Mockito.times(1)).findByStudentId(42L);
        Mockito.verify(enrollmentService, Mockito.never()).findAll();
    }

    @Test
    void testGetMyEnrollmentsForbiddenForNonStudent() throws Exception {
        mockMvc.perform(get("/v1/me/enrollments").with(adminJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/me/enrollments").with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }
}
