package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.model.Education;
import me.ronygomes.ums.api.model.Student;
import me.ronygomes.ums.api.service.StudentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{id}")
    public Student findById(@PathVariable Long id) {
        return studentService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Student student) {
        long newId = studentService.create(student);
        return ResponseEntity.created(linkTo(StudentController.class).slash(newId).toUri())
                .build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Student student) {
        studentService.updateAll(id, student);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(StudentController.class).slash(id).toUri().toString())
                .build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePatch(@PathVariable Long id, @RequestBody Student student) {
        studentService.updateProvided(id, student);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(StudentController.class).slash(id).toUri().toString())
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/educations")
    public ResponseEntity<?> addEducation(@PathVariable Long id, @RequestBody Education education) {
        studentService.addEducation(id, education);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(StudentController.class).slash(id).toUri().toString())
                .build();
    }

    @PutMapping("/{id}/educations/{eid}")
    public ResponseEntity<?> updateEducation(@PathVariable Long id,
                                             @PathVariable Long eid,
                                             @RequestBody Education education) {

        studentService.updateEducation(id, eid, education);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(StudentController.class).slash(id).toUri().toString())
                .build();
    }

    @PatchMapping("/{id}/educations/{eid}")
    public ResponseEntity<?> updatePatchEducation(@PathVariable Long id,
                                                  @PathVariable Long eid,
                                                  @RequestBody Education education) {

        studentService.updatePatchEducation(id, eid, education);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(StudentController.class).slash(id).toUri().toString())
                .build();
    }

    @DeleteMapping("/{id}/educations/{eid}")
    public ResponseEntity<?> deleteEducation(@PathVariable Long id, @PathVariable Long eid) {
        studentService.deleteEducation(id, eid);
        return ResponseEntity.accepted().build();
    }
}
