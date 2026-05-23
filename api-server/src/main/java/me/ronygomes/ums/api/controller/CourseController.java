package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.config.annotation.AdminAccess;
import me.ronygomes.ums.api.dto.CourseDto;
import me.ronygomes.ums.api.dto.CoursePatchDto;
import me.ronygomes.ums.api.service.CourseService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping
    public List<CourseDto> findAll() {
        return courseService.findAll();
    }

    @AdminAccess
    @GetMapping("/{id}")
    public CourseDto findById(@PathVariable Long id) {
        return courseService.findById(id);
    }

    @AdminAccess
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CourseDto course) {
        long newId = courseService.create(course);
        return ResponseEntity.created(linkTo(CourseController.class).slash(newId).toUri())
                .build();
    }

    @AdminAccess
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CourseDto courseDto) {
        courseService.updateAll(id, courseDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(CourseController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePatch(@PathVariable Long id, @RequestBody CoursePatchDto courseDto) {
        courseService.updateProvided(id, courseDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(CourseController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.accepted().build();
    }
}
