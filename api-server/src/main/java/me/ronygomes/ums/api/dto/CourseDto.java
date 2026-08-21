package me.ronygomes.ums.api.dto;

import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Semester;
import me.ronygomes.ums.api.model.Teacher;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

public class CourseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull
    @Size(min = 1, max = 20)
    private String code;

    @NotNull
    @Size(min = 1, max = 200)
    private String name;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private BigDecimal credit;

    @Size(max = 2000)
    private String description;

    @NotNull
    private String departmentCode;

    @NotNull
    @Enumerated(STRING)
    private Semester semester;

    private Long instructorId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public static CourseDto toDto(Course entity) {
        CourseDto dto = new CourseDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setCredit(BigDecimal.valueOf(entity.getCredit()));
        dto.setDescription(entity.getDescription());
        dto.setDepartmentCode(entity.getDepartment().getCode());
        dto.setSemester(entity.getSemester());

        if (Objects.nonNull(entity.getInstructor())) {
            dto.setInstructorId(entity.getInstructor().getId());
        }

        return dto;
    }

    public void copy(Course to, Department department, Teacher instructor) {
        to.setCode(getCode());
        to.setName(getName());
        to.setCredit(getCredit().floatValue());
        to.setDescription(getDescription());
        to.setDepartment(department);
        to.setSemester(getSemester());
        to.setInstructor(instructor);
    }
}
