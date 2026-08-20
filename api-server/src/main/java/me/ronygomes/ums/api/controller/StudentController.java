package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.config.annotation.AdminAccess;
import me.ronygomes.ums.api.dto.PagedResponse;
import me.ronygomes.ums.api.model.Education;
import me.ronygomes.ums.api.model.Student;
import me.ronygomes.ums.api.service.StudentService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @AdminAccess
    @GetMapping
    public PagedResponse<Student> findPaged(Pageable pageable) {
        return PagedResponse.of(studentService.findPaged(pageable));
    }

    @AdminAccess
    @GetMapping("/{id}")
    public Student findById(@PathVariable Long id) {
        return studentService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Student student) {
        long newId = studentService.create(student, new Date());
        return ResponseEntity.created(linkTo(StudentController.class).slash(newId).toUri())
                .body(Map.of(
                        "id", newId,
                        "registrationNumber", student.getRegistrationNumber()
                ));
    }

    @AdminAccess
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Student student) {
        studentService.updateAll(id, student);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(StudentController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePatch(@PathVariable Long id, @RequestBody Student student) {
        studentService.updateProvided(id, student);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(StudentController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.accepted().build();
    }

    @AdminAccess
    @PostMapping("/{id}/educations")
    public ResponseEntity<?> addEducation(@PathVariable Long id, @RequestBody Education education) {
        studentService.addEducation(id, education);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(StudentController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @PutMapping("/{id}/educations/{eid}")
    public ResponseEntity<?> updateEducation(@PathVariable Long id,
                                             @PathVariable Long eid,
                                             @RequestBody Education education) {

        studentService.updateEducation(id, eid, education);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(StudentController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @PatchMapping("/{id}/educations/{eid}")
    public ResponseEntity<?> updatePatchEducation(@PathVariable Long id,
                                                  @PathVariable Long eid,
                                                  @RequestBody Education education) {

        studentService.updatePatchEducation(id, eid, education);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(StudentController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @DeleteMapping("/{id}/educations/{eid}")
    public ResponseEntity<?> deleteEducation(@PathVariable Long id, @PathVariable Long eid) {
        studentService.deleteEducation(id, eid);
        return ResponseEntity.accepted().build();
    }
}
