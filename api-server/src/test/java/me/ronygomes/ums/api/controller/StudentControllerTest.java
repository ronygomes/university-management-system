package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.*;
import me.ronygomes.ums.api.service.StudentService;
import me.ronygomes.ums.api.testHelper.DataHelper;
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

import static me.ronygomes.ums.api.testHelper.RoleHelper.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("controller-test")
public class StudentControllerTest {

    String JSON_DATA = """
            {
                "id": 100,
                "fullName": "John Doe",
                "email": "john@example.com",
                "contactNumber": "+1111111111111",
                "address": "Street 123",
                "departmentCode": "CSE",
                "registrationDate": "2024-09-04T17:03:43.849+00:00",
                "registrationNumber": "123",
                "educations": [
                    {
                        "id": 200,
                        "examType": "HSC",
                        "grade": "A",
                        "cgpa": 3.92,
                        "certificateFileName": "hsc-2024-certificate.pdf",
                        "certificatePath": "e1b4c5f8-7927-4aee-a010-c81ec3ca00de"
                    }
                ]
            }
            """;

    @MockBean
    StudentService studentService;

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
    void testFindById() throws Exception {
        Mockito.when(studentService.findById(9L)).thenReturn(mockDBStudent());

        mockMvc.perform(get("/v1/students/9")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Student 1"))
                .andExpect(jsonPath("$.email").value("student1@example.com"))
                .andExpect(jsonPath("$.contactNumber").value("+5501327738765"))
                .andExpect(jsonPath("$.departmentCode").value("CODE-1"))
                .andExpect(jsonPath("$.address").value("Some Student Address"))
                .andExpect(jsonPath("$.registrationDate").exists())
                .andExpect(jsonPath("$.registrationNumber").value("2024-CSE-0001"))
                .andExpect(jsonPath("$.educations[0].id").value(3))
                .andExpect(jsonPath("$.educations[0].examType").value(ExamType.SSC.name()))
                .andExpect(jsonPath("$.educations[0].grade").value(Grade.A.name()))
                .andExpect(jsonPath("$.educations[0].cgpa").value(Grade.A.getGpa()))
                .andExpect(jsonPath("$.educations[0].certificateFileName").value("ssc-certificate.pdf"))
                .andExpect(jsonPath("$.educations[0].certificatePath").value("some-random-path/2024-CSE-0001/ssc-certificate.pdf"))
                .andExpect(jsonPath("$.educations[1].id").value(4))
                .andExpect(jsonPath("$.educations[1].examType").value(ExamType.HSC.name()))
                .andExpect(jsonPath("$.educations[1].grade").value(Grade.A_PLUS.name()))
                .andExpect(jsonPath("$.educations[1].cgpa").value(Grade.A_PLUS.getGpa()))
                .andExpect(jsonPath("$.educations[1].certificateFileName").value("hsc-certificate.pdf"))
                .andExpect(jsonPath("$.educations[1].certificatePath").value("some-random-path/2024-CSE-0001/hsc-certificate.pdf"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/students/9")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/students/9")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testFindByIdFailure() throws Exception {
        UmsDataException ex = new UmsDataException(ExceptionType.ENTITY_NOT_FOUND, "abc");
        Mockito.when(studentService.findById(1L)).thenThrow(ex);

        mockMvc.perform(get("/v1/students/1")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$.length()").value("4"))
                .andExpect(jsonPath("$.type").value("https://documentation.com/errors/entity-not-found"))
                .andExpect(jsonPath("$.title").value("Requested object not found"))
                .andExpect(jsonPath("$.detail").value("abc"))
                .andExpect(jsonPath("$.instance").value("/v1/students/1"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/students/1")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(get("/v1/students/1")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testCreate() throws Exception {
        ArgumentCaptor<Student> ac = ArgumentCaptor.forClass(Student.class);
        Mockito.when(studentService.create(ac.capture(), Mockito.any())).thenReturn(501L);

        mockMvc.perform(post("/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATA)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/students/501"))
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().isCreated());

        Student s = ac.getValue();
        Assertions.assertNull(s.getId()); // Can't bind
        Assertions.assertEquals("John Doe", s.getFullName());
        Assertions.assertEquals("john@example.com", s.getEmail());
        Assertions.assertEquals("+1111111111111", s.getContactNumber());
        Assertions.assertEquals("Street 123", s.getAddress());
        Assertions.assertNull(s.getDepartment());
        Assertions.assertEquals("CSE", s.getDepartmentCode());
        Assertions.assertNull(s.getRegistrationDate()); // Can't bind
        Assertions.assertNull(s.getRegistrationNumber()); // Can't bind
        Assertions.assertEquals(1, s.getEducations().size());
        Assertions.assertNull(s.getEducations().get(0).getId()); // Can't bind
        Assertions.assertEquals(ExamType.HSC, s.getEducations().get(0).getExamType());
        Assertions.assertEquals(Grade.A, s.getEducations().get(0).getGrade());
        Assertions.assertEquals(0, Float.compare(s.getEducations().get(0).getCgpa(), 3.92f));
        Assertions.assertEquals("hsc-2024-certificate.pdf", s.getEducations().get(0).getCertificateFileName());
        Assertions.assertEquals("e1b4c5f8-7927-4aee-a010-c81ec3ca00de", s.getEducations().get(0).getCertificatePath());

        mockMvc.perform(post("/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATA)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.CREATED.value()));

        mockMvc.perform(post("/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATA)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.CREATED.value()));
    }

    @Test
    void testUpdate() throws Exception {
        ArgumentCaptor<Student> ac = ArgumentCaptor.forClass(Student.class);
        Mockito.doNothing().when(studentService).updateAll(Mockito.eq(1L), ac.capture());

        mockMvc.perform(put("/v1/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATA)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/students/1"))
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().isAccepted());

        Student s = ac.getValue();
        Assertions.assertNull(s.getId()); // Can't bind
        Assertions.assertEquals("John Doe", s.getFullName());
        Assertions.assertEquals("john@example.com", s.getEmail());
        Assertions.assertEquals("+1111111111111", s.getContactNumber());
        Assertions.assertEquals("Street 123", s.getAddress());
        Assertions.assertNull(s.getDepartment());
        Assertions.assertEquals("CSE", s.getDepartmentCode());
        Assertions.assertNull(s.getRegistrationDate()); // Can't bind
        Assertions.assertNull(s.getRegistrationNumber()); // Can't bind
        Assertions.assertEquals(1, s.getEducations().size());
        Assertions.assertNull(s.getEducations().get(0).getId()); // Can't bind
        Assertions.assertEquals(ExamType.HSC, s.getEducations().get(0).getExamType());
        Assertions.assertEquals(Grade.A, s.getEducations().get(0).getGrade());
        Assertions.assertEquals(0, Float.compare(s.getEducations().get(0).getCgpa(), 3.92f));
        Assertions.assertEquals("hsc-2024-certificate.pdf", s.getEducations().get(0).getCertificateFileName());
        Assertions.assertEquals("e1b4c5f8-7927-4aee-a010-c81ec3ca00de", s.getEducations().get(0).getCertificatePath());

        mockMvc.perform(put("/v1/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATA)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(put("/v1/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_DATA)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testPatch() throws Exception {
        ArgumentCaptor<Student> ac = ArgumentCaptor.forClass(Student.class);
        Mockito.doNothing().when(studentService).updateAll(Mockito.eq(1L), ac.capture());

        String patchJson = """
                {
                    "id": 100,
                    "email": "john@example.com",
                    "departmentCode": "CSE",
                    "registrationDate": "2024-09-04T17:03:43.849+00:00",
                    "registrationNumber": "123",
                    "educations": [
                        {
                            "id": 200,
                            "examType": "HSC",
                            "grade": "A",
                            "cgpa": 3.92
                        }
                    ]
                }
                """;

        mockMvc.perform(put("/v1/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/students/1"))
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().isAccepted());

        Student s = ac.getValue();
        Assertions.assertNull(s.getId()); // Can't bind
        Assertions.assertNull(s.getFullName());
        Assertions.assertEquals("john@example.com", s.getEmail());
        Assertions.assertNull(s.getContactNumber());
        Assertions.assertNull(s.getAddress());
        Assertions.assertNull(s.getDepartment());
        Assertions.assertEquals("CSE", s.getDepartmentCode());
        Assertions.assertNull(s.getRegistrationDate()); // Can't bind
        Assertions.assertNull(s.getRegistrationNumber()); // Can't bind
        Assertions.assertEquals(1, s.getEducations().size());
        Assertions.assertNull(s.getEducations().get(0).getId()); // Can't bind
        Assertions.assertEquals(ExamType.HSC, s.getEducations().get(0).getExamType());
        Assertions.assertEquals(Grade.A, s.getEducations().get(0).getGrade());
        Assertions.assertEquals(0, Float.compare(s.getEducations().get(0).getCgpa(), 3.92f));
        Assertions.assertNull(s.getEducations().get(0).getCertificateFileName());
        Assertions.assertNull(s.getEducations().get(0).getCertificatePath());

        mockMvc.perform(put("/v1/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(put("/v1/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/v1/students/5")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().isAccepted());

        Mockito.verify(studentService, Mockito.times(1)).delete(5L);

        mockMvc.perform(delete("/v1/students/5")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(delete("/v1/students/5")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testAddEducations() throws Exception {
        ArgumentCaptor<Education> ac = ArgumentCaptor.forClass(Education.class);
        Mockito.doNothing().when(studentService).addEducation(Mockito.eq(5L), ac.capture());

        String educationJson = """
                {
                    "id": 200,
                    "examType": "HSC",
                    "grade": "A",
                    "cgpa": 3.92,
                    "certificateFileName": "hsc-2024-certificate.pdf",
                    "certificatePath": "e1b4c5f8-7927-4aee-a010-c81ec3ca00de"
                }
                """;

        mockMvc.perform(post("/v1/students/5/educations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/students/5"))
                .andExpect(status().isAccepted());

        Education e = ac.getValue();
        Assertions.assertNull(e.getId()); // Can't bind
        Assertions.assertEquals(ExamType.HSC, e.getExamType());
        Assertions.assertEquals(Grade.A, e.getGrade());
        Assertions.assertEquals(0, Float.compare(e.getCgpa(), 3.92f));
        Assertions.assertEquals("hsc-2024-certificate.pdf", e.getCertificateFileName());
        Assertions.assertEquals("e1b4c5f8-7927-4aee-a010-c81ec3ca00de", e.getCertificatePath());

        mockMvc.perform(post("/v1/students/5/educations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(post("/v1/students/5/educations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testUpdateEducation() throws Exception {
        ArgumentCaptor<Education> ac = ArgumentCaptor.forClass(Education.class);
        Mockito.doNothing().when(studentService).updateEducation(Mockito.eq(5L), Mockito.eq(56L), ac.capture());

        String educationJson = """
                {
                    "id": 200,
                    "examType": "HSC",
                    "grade": "A",
                    "cgpa": 3.92,
                    "certificateFileName": "hsc-2024-certificate.pdf",
                    "certificatePath": "e1b4c5f8-7927-4aee-a010-c81ec3ca00de"
                }
                """;

        mockMvc.perform(put("/v1/students/5/educations/56")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/students/5"))
                .andExpect(status().isAccepted());

        Education e = ac.getValue();
        Assertions.assertNull(e.getId()); // Can't bind
        Assertions.assertEquals(ExamType.HSC, e.getExamType());
        Assertions.assertEquals(Grade.A, e.getGrade());
        Assertions.assertEquals(0, Float.compare(e.getCgpa(), 3.92f));
        Assertions.assertEquals("hsc-2024-certificate.pdf", e.getCertificateFileName());
        Assertions.assertEquals("e1b4c5f8-7927-4aee-a010-c81ec3ca00de", e.getCertificatePath());

        mockMvc.perform(put("/v1/students/5/educations/56")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(put("/v1/students/5/educations/56")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testUpdatePatchEducation() throws Exception {
        ArgumentCaptor<Education> ac = ArgumentCaptor.forClass(Education.class);
        Mockito.doNothing().when(studentService).updatePatchEducation(Mockito.eq(5L), Mockito.eq(56L), ac.capture());

        String educationJson = """
                {
                    "id": 200,
                    "examType": "HSC",
                    "cgpa": 3.92
                }
                """;

        mockMvc.perform(patch("/v1/students/5/educations/56")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/v1/students/5"))
                .andExpect(status().isAccepted());

        Education e = ac.getValue();
        Assertions.assertNull(e.getId()); // Can't bind
        Assertions.assertEquals(ExamType.HSC, e.getExamType());
        Assertions.assertNull(e.getGrade());
        Assertions.assertEquals(0, Float.compare(e.getCgpa(), 3.92f));
        Assertions.assertNull(e.getCertificateFileName());
        Assertions.assertNull(e.getCertificatePath());

        mockMvc.perform(patch("/v1/students/5/educations/56")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(patch("/v1/students/5/educations/56")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testDeleteEducation() throws Exception {
        mockMvc.perform(delete("/v1/students/5/educations/34")
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(status().isAccepted());

        Mockito.verify(studentService, Mockito.times(1)).deleteEducation(5L, 34L);

        mockMvc.perform(delete("/v1/students/5/educations/34")
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(delete("/v1/students/5/educations/34")
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    private Student mockDBStudent() {
        Department d = DataHelper.validPersistableDepartment1();
        d.setId(2L);

        Student s = DataHelper.validPersistableStudent1(d);
        s.setId(1L);

        Assertions.assertEquals(2, s.getEducations().size());
        s.getEducations().get(0).setId(3L);
        s.getEducations().get(1).setId(4L);

        return s;
    }
}
