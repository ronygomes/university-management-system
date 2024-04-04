package me.ronygomes.ums.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.ronygomes.ums.api.model.Department;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serializable;

@Relation(collectionRelation = "departments", itemRelation = "department")
public class DepartmentDto implements Serializable {

    @NotNull
    @Size(min = 1, max = 10)
    private String code;

    @NotNull
    @Size(min = 1, max = 100)
    private String name;

    public DepartmentDto() {
    }

    public DepartmentDto(Department department) {
        this.code = department.getCode();
        this.name = department.getName();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department toDepartment() {
        Department entity = new Department();
        entity.setName(getName());
        entity.setCode(getCode());
        return entity;
    }
}
