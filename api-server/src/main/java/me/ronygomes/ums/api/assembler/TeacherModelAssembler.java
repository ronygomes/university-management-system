package me.ronygomes.ums.api.assembler;

import me.ronygomes.ums.api.dto.DepartmentDto;
import me.ronygomes.ums.api.dto.DesignationModel;
import me.ronygomes.ums.api.dto.TeacherOutputDto;
import me.ronygomes.ums.api.model.Teacher;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class TeacherModelAssembler implements RepresentationModelAssembler<Teacher, TeacherOutputDto> {

    @Override
    public TeacherOutputDto toModel(Teacher entity) {
        return convertToOutputDto(entity);
    }

    @Override
    public CollectionModel<TeacherOutputDto> toCollectionModel(Iterable<? extends Teacher> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }

    private TeacherOutputDto convertToOutputDto(Teacher t) {
        TeacherOutputDto dto = new TeacherOutputDto();
        dto.setFullName(t.getFullName());
        dto.setAddress(t.getAddress());
        dto.setEmail(t.getEmail());
        dto.setContactNumber(t.getContactNumber());
        dto.setAssignedCredit(t.getAssignedCredit());
        dto.setDesignation(new DesignationModel(t.getDesignation()));
        dto.setDepartment(EntityModel.of(new DepartmentDto(t.getDepartment())));

        return dto;
    }
}
