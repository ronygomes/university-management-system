package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.assembler.DesignationModelAssembler;
import me.ronygomes.ums.api.dto.DesignationModel;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.Designation;
import me.ronygomes.ums.api.repository.DesignationRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1/designations")
public class DesignationController {

    private static final String FIND_BY_ID_ERROR_DETAILS_TEMPLATE = "Designation with id '%d' not found";

    private final DesignationRepository designationRepository;
    private final DesignationModelAssembler designationModelAssembler;

    public DesignationController(DesignationRepository designationRepository,
                                 DesignationModelAssembler designationModelAssembler) {
        this.designationRepository = designationRepository;
        this.designationModelAssembler = designationModelAssembler;
    }

    @GetMapping
    public CollectionModel<DesignationModel> designations() {
        List<Designation> designations = designationRepository.findAll();
        return designationModelAssembler.toCollectionModel(designations);
    }

    @GetMapping("/{id}")
    public DesignationModel designation(@PathVariable Long id) {
        Designation designation = findDesignationOrThrow(id);
        return designationModelAssembler.toModel(designation);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DesignationModel designation) {
        Designation dBDesignation = new Designation();
        dBDesignation.setTitle(designation.getTitle());

        designationRepository.save(dBDesignation);

        return ResponseEntity.created(linkTo(DesignationController.class).slash(dBDesignation.getId()).toUri())
                .build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DesignationModel designation) {
        Designation dBDesignation = findDesignationOrThrow(id);
        dBDesignation.setTitle(designation.getTitle());

        designationRepository.save(dBDesignation);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, linkTo(DesignationController.class).slash(dBDesignation.getId()).toString())
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        designationRepository.delete(findDesignationOrThrow(id));
        return ResponseEntity.accepted().build();
    }

    private Designation findDesignationOrThrow(Long id) {
        return designationRepository.findById(id)
                .orElseThrow(() -> new UmsDataException(ExceptionType.ENTITY_NOT_FOUND,
                        FIND_BY_ID_ERROR_DETAILS_TEMPLATE.formatted(id)));
    }
}
