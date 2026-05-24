package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.dto.EnrollmentDto;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.Student;
import me.ronygomes.ums.api.model.Teacher;
import me.ronygomes.ums.api.service.EnrollmentService;
import me.ronygomes.ums.api.service.StudentService;
import me.ronygomes.ums.api.service.TeacherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/me")
public class MeController {

    private static final String STUDENT_NOT_FOUND_TEMPLATE = "No student record for email '%s'";
    private static final String TEACHER_NOT_FOUND_TEMPLATE = "No teacher record for email '%s'";

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final EnrollmentService enrollmentService;

    public MeController(StudentService studentService,
                        TeacherService teacherService,
                        EnrollmentService enrollmentService) {

        this.studentService = studentService;
        this.teacherService = teacherService;
        this.enrollmentService = enrollmentService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student")
    public Student getMyStudent(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return studentService.findByEmail(email)
                .orElseThrow(() -> new UmsDataException(ExceptionType.ENTITY_NOT_FOUND,
                        STUDENT_NOT_FOUND_TEMPLATE.formatted(email)));
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/teacher")
    public Teacher getMyTeacher(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return teacherService.findByEmail(email)
                .orElseThrow(() -> new UmsDataException(ExceptionType.ENTITY_NOT_FOUND,
                        TEACHER_NOT_FOUND_TEMPLATE.formatted(email)));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/enrollments")
    public List<EnrollmentDto> getMyEnrollments(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        Long studentId = studentService.findByEmail(email)
                .orElseThrow(() -> new UmsDataException(ExceptionType.ENTITY_NOT_FOUND,
                        STUDENT_NOT_FOUND_TEMPLATE.formatted(email)))
                .getId();
        return enrollmentService.findByStudentId(studentId);
    }
}
