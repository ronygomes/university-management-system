package me.ronygomes.ums.api.dto;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "teachers", itemRelation = "teacher")
public class TeacherOutputDto extends AbstractTeacherBaseDto<TeacherOutputDto>  {

    private EntityModel<DepartmentDto> department;
    private DesignationModel designation;

    public EntityModel<DepartmentDto> getDepartment() {
        return department;
    }

    public void setDepartment(EntityModel<DepartmentDto> department) {
        this.department = department;
    }

    public DesignationModel getDesignation() {
        return designation;
    }

    public void setDesignation(DesignationModel designation) {
        this.designation = designation;
    }
}
