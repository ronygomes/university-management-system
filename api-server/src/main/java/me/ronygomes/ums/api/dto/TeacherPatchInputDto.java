package me.ronygomes.ums.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import me.ronygomes.ums.api.model.Teacher;

import java.io.Serializable;
import java.util.Objects;

import static me.ronygomes.ums.api.model.AbstractEntity.EMAIL_REGEX_PATTERN;
import static me.ronygomes.ums.api.model.AbstractEntity.PHONE_REGEX_PATTERN;

public class TeacherPatchInputDto implements Serializable {

    @Size(min = 1, max = 200)
    private String fullName;

    @Size(max = 1000)
    private String address;

    @Size(min = 5, max = 100)
    @Pattern(regexp = EMAIL_REGEX_PATTERN, message = "invalid email format")
    private String email;

    @Pattern(regexp = PHONE_REGEX_PATTERN, message = "invalid contact number format")
    private String contactNumber;

    @Min(0)
    @Max(100)
    private Float assignedCredit;

    @Size(min = 1, max = 100)
    private String title;

    @Size(min = 1, max = 10)
    private String departmentCode;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Float getAssignedCredit() {
        return assignedCredit;
    }

    public void setAssignedCredit(Float assignedCredit) {
        this.assignedCredit = assignedCredit;
    }

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

    public TeacherDto toInputDto(Teacher dbData) {
        TeacherDto res = new TeacherDto();
        if (Objects.nonNull(getFullName())) {
            res.setFullName(getFullName());
        } else {
            res.setFullName(dbData.getFullName());
        }

        if (Objects.nonNull(getAddress())) {
            res.setAddress(getAddress());
        } else {
            res.setAddress(dbData.getAddress());
        }

        if (Objects.nonNull(getEmail())) {
            res.setEmail(getEmail());
        } else {
            res.setEmail(dbData.getEmail());
        }

        if (Objects.nonNull(getContactNumber())) {
            res.setContactNumber(getContactNumber());
        } else {
            res.setContactNumber(dbData.getContactNumber());
        }

        if (Objects.nonNull(getAssignedCredit())) {
            res.setAssignedCredit(getAssignedCredit());
        } else {
            res.setAssignedCredit(dbData.getAssignedCredit());
        }

        if (Objects.nonNull(getDepartmentCode())) {
            res.setDepartmentCode(getDepartmentCode());
        } else {
            res.setDepartmentCode(dbData.getDepartment().getCode());
        }

        if (Objects.nonNull(getTitle())) {
            res.setTitle(getTitle());
        } else {
            res.setTitle(dbData.getDesignation().getTitle());
        }

        return res;
    }
}
