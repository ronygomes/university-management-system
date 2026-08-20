package me.ronygomes.ums.api.assembler;

import me.ronygomes.ums.api.controller.DepartmentController;
import me.ronygomes.ums.api.controller.DesignationController;
import me.ronygomes.ums.api.controller.TeacherController;
import me.ronygomes.ums.api.dto.DesignationModel;
import me.ronygomes.ums.api.dto.TeacherDto;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Teacher;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static me.ronygomes.ums.api.helper.HalDataExcluder.HalDataOutputType;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TeacherModelHelper {

    private static final TeacherController teacherController = methodOn(TeacherController.class);
    private static final DesignationController designationController = methodOn(DesignationController.class);
    private static final DepartmentController departmentController = methodOn(DepartmentController.class);

    public RepresentationModel<TeacherDto> toModel(Teacher teacher) {
        TeacherDto dto = convertToOutputDto(teacher, HalDataOutputType.FULL);
        dto.add(linkTo(teacherController.getById(teacher.getId())).withSelfRel());

        DesignationModel dm = new DesignationModel(teacher.getDesignation());
        dm.add(linkTo(designationController.designation(teacher.getDesignation().getId())).withSelfRel());

        EntityModel<Department> d = EntityModel.of(teacher.getDepartment());
        d.add(linkTo(departmentController.department(dto.getDepartmentCode())).withSelfRel());

        return HalModelBuilder
                .halModelOf(dto)
                .embed(dm)
                .embed(d)
                .build();
    }

    public TeacherDto toEmbeddedModel(Teacher entity) {
        TeacherDto dto = convertToOutputDto(entity, HalDataOutputType.EMBEDDED);
        dto.add(linkTo(departmentController.department(dto.getDepartmentCode())).withRel("department"));
        dto.add(linkTo(designationController.designation(entity.getDesignation().getId())).withRel("designation"));
        dto.add(linkTo(teacherController.getById(entity.getId())).withSelfRel());

        return dto;
    }

    public CollectionModel<TeacherDto> toCollectionModel(Iterable<? extends Teacher> entities) {
        List<TeacherDto> teachers = new ArrayList<>();
        for (Teacher entity : entities) {
            teachers.add(toEmbeddedModel(entity));
        }

        CollectionModel<TeacherDto> col = CollectionModel.of(teachers);
        col.add(linkTo(teacherController.getAll()).withSelfRel());

        return col;
    }

    private TeacherDto convertToOutputDto(Teacher t, HalDataOutputType type) {
        TeacherDto dto = new TeacherDto(type);

        dto.setFullName(t.getFullName());
        dto.setTitle(t.getDesignation().getTitle());
        dto.setDepartmentCode(t.getDepartment().getCode());
        dto.setEmail(t.getEmail());
        dto.setAddress(t.getAddress());
        dto.setContactNumber(t.getContactNumber());
        dto.setAssignedCredit(t.getAssignedCredit());

        return dto;
    }
}
