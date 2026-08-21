package me.ronygomes.ums.api.dto;

import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.Semester;
import me.ronygomes.ums.api.model.Teacher;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

public class CoursePatchDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(min = 1, max = 20)
    private String code;

    @Size(min = 1, max = 200)
    private String name;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private BigDecimal credit;

    @Size(max = 2000)
    private String description;

    private String departmentCode;

    @Enumerated(STRING)
    private Semester semester;

    private List<Long> instructorIds;

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

    public List<Long> getInstructorIds() {
        return instructorIds;
    }

    public void setInstructorIds(List<Long> instructorIds) {
        this.instructorIds = instructorIds;
    }

    public CourseDto toInputDto(Course dbData) {
        CourseDto res = new CourseDto();
        res.setCode(Objects.nonNull(getCode()) ? getCode() : dbData.getCode());
        res.setName(Objects.nonNull(getName()) ? getName() : dbData.getName());
        res.setCredit(Objects.nonNull(getCredit()) ? getCredit() : BigDecimal.valueOf(dbData.getCredit()));
        res.setDescription(Objects.nonNull(getDescription()) ? getDescription() : dbData.getDescription());
        res.setDepartmentCode(Objects.nonNull(getDepartmentCode()) ? getDepartmentCode() : dbData.getDepartment().getCode());
        res.setSemester(Objects.nonNull(getSemester()) ? getSemester() : dbData.getSemester());

        if (Objects.nonNull(getInstructorIds())) {
            res.setInstructorIds(getInstructorIds());
        } else {
            res.setInstructorIds(dbData.getInstructors().stream()
                    .map(Teacher::getId)
                    .sorted()
                    .toList());
        }

        return res;
    }
}
