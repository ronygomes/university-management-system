package me.ronygomes.ums.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serial;

@Relation(collectionRelation = "teachers", itemRelation = "teacher")
public class TeacherInputDto extends AbstractTeacherBaseDto<TeacherInputDto> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Size(min = 1, max = 100)
    private String title;

    @NotNull
    @Size(min = 1, max = 10)
    private String departmentCode;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
}
