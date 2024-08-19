package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.dto.DepartmentDto;
import me.ronygomes.ums.api.service.DepartmentService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public CollectionModel<EntityModel<DepartmentDto>> departments() {
        List<DepartmentDto> departments = departmentService.findAll();
        List<EntityModel<DepartmentDto>> models = departments
                .stream()
                .map(department -> EntityModel.of(department, createDepartmentLinks(department)))
                .toList();

        Link link = linkTo(DepartmentController.class).withSelfRel()
                .andAffordance(afford(methodOn(DepartmentController.class).create(new DepartmentDto())));

        return CollectionModel.of(models, Collections.singleton(link));
    }

    @GetMapping("/{code}")
    public EntityModel<DepartmentDto> department(@PathVariable String code) {
        EntityModel<DepartmentDto> model = EntityModel.of(departmentService.findByCode(code));
        var selfLink = linkTo(methodOn(DepartmentController.class).department(code)).withSelfRel()
                .andAffordance(afford(methodOn(DepartmentController.class).updateAll(code, new DepartmentDto())));

        Link selfLinkWithAffordances = Affordances.of(selfLink)
                .afford(HttpMethod.PATCH)
                .withName("patchDepartment")
                .withInputAndOutput(DepartmentDto.class)
                .addParameters(QueryParameter.optional("code"), QueryParameter.optional("name"))
                .toLink();

        model.add(selfLinkWithAffordances);
        return model;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DepartmentDto department) {
        departmentService.save(department);
        return ResponseEntity.created(createDepartmentUri(department)).build();
    }

    @PutMapping("/{code}")
    public ResponseEntity<?> updateAll(@PathVariable String code, @RequestBody DepartmentDto department) {
        departmentService.updateAll(code, department);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, createDepartmentUri(department).toASCIIString())
                .build();
    }

    @PatchMapping("/{code}")
    public ResponseEntity<?> updateOne(@PathVariable String code, @RequestBody DepartmentDto department) {
        departmentService.updateOne(code, department);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, createDepartmentUri(department).toASCIIString())
                .build();
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<?> delete(@PathVariable String code) {
        departmentService.delete(code);
        return ResponseEntity.accepted().build();
    }

    private Link createDepartmentLinks(DepartmentDto department) {
        return linkTo(methodOn(DepartmentController.class).department(department.getCode())).withRel("department");
    }

    private URI createDepartmentUri(DepartmentDto department) {
        return linkTo(DepartmentController.class).slash(department.getCode()).toUri();
    }
}
