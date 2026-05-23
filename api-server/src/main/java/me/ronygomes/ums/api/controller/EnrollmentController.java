package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.config.annotation.AdminAccess;
import me.ronygomes.ums.api.dto.EnrollmentDto;
import me.ronygomes.ums.api.service.EnrollmentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping
    public List<EnrollmentDto> findAll() {
        return enrollmentService.findAll();
    }

    @AdminAccess
    @GetMapping("/{id}")
    public EnrollmentDto findById(@PathVariable Long id) {
        return enrollmentService.findById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody EnrollmentDto enrollmentDto) {
        long newId = enrollmentService.create(enrollmentDto);
        return ResponseEntity.created(linkTo(EnrollmentController.class).slash(newId).toUri())
                .build();
    }

    @AdminAccess
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody EnrollmentDto enrollmentDto) {
        enrollmentService.update(id, enrollmentDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(EnrollmentController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateProvided(@PathVariable Long id, @RequestBody EnrollmentDto enrollmentDto) {
        enrollmentService.updateProvided(id, enrollmentDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(EnrollmentController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        enrollmentService.delete(id);
        return ResponseEntity.accepted().build();
    }
}
