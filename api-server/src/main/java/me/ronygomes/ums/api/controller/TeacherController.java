package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.assembler.TeacherModelHelper;
import me.ronygomes.ums.api.config.annotation.AdminAccess;
import me.ronygomes.ums.api.dto.TeacherDto;
import me.ronygomes.ums.api.dto.TeacherPatchInputDto;
import me.ronygomes.ums.api.service.TeacherService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1/teachers")
public class TeacherController {

    private TeacherService teacherService;
    private TeacherModelHelper teacherModelAssembler;

    public TeacherController(TeacherService teacherService,
                             TeacherModelHelper teacherModelAssembler) {
        this.teacherService = teacherService;
        this.teacherModelAssembler = teacherModelAssembler;
    }

    @GetMapping
    @AdminAccess
    public CollectionModel<TeacherDto> getAll() {
        return teacherModelAssembler.toCollectionModel(teacherService.findAll());
    }

    @AdminAccess
    @GetMapping("/{id}")
    public RepresentationModel<TeacherDto> getById(@PathVariable long id) {
        return teacherModelAssembler.toModel(teacherService.findById(id));
    }

    @AdminAccess
    @PostMapping
    public ResponseEntity<?> create(@RequestBody TeacherDto teacher) {
        long newId = teacherService.create(teacher);
        return ResponseEntity.created(linkTo(TeacherController.class).slash(newId).toUri())
                .build();
    }

    @AdminAccess
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TeacherDto teacherDto) {
        teacherService.updateAll(id, teacherDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(TeacherController.class).slash(id).toUri().toString())
                .build();
    }

    @AdminAccess
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePatch(@PathVariable Long id, @RequestBody TeacherPatchInputDto teacherDto) {
        teacherService.updateProvided(id, teacherDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(TeacherController.class).slash(id).toUri().toString())
                .build();
    }


    @AdminAccess
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.accepted().build();
    }
}
