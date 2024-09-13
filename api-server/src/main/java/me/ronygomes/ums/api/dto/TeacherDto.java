package me.ronygomes.ums.api.dto;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import me.ronygomes.ums.api.helper.HalDataExcluder;
import me.ronygomes.ums.api.validator.annotation.ContactNumber;
import me.ronygomes.ums.api.validator.annotation.Email;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

@Relation(collectionRelation = "teachers", itemRelation = "teacher")
@JsonFilter(HalDataExcluder.FILTER_NAME)
public class TeacherDto extends RepresentationModel<TeacherDto> implements HalDataExcluder {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Set<String> HAL_FULL_ONLY_FIELDS = new HashSet<>();

    static {
        HAL_FULL_ONLY_FIELDS.add("address");
        HAL_FULL_ONLY_FIELDS.add("contactNumber");
        HAL_FULL_ONLY_FIELDS.add("assignedCredit");
    }

    public TeacherDto() {
    }

    public TeacherDto(HalDataOutputType type) {
        this.type = type;
    }

    @NotNull
    @Size(min = 1, max = 200)
    private String fullName;

    @Email
    @NotNull
    @Size(min = 5, max = 100)
    private String email;

    @NotNull
    @Size(min = 1, max = 100)
    private String title;

    @NotNull
    @Size(min = 1, max = 10)
    private String departmentCode;

    @Size(max = 1000)
    private String address;

    @ContactNumber
    private String contactNumber;

    @Min(0)
    @Max(100)
    private float assignedCredit;

    @JsonIgnore
    private HalDataOutputType type;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public float getAssignedCredit() {
        return assignedCredit;
    }

    public void setAssignedCredit(float assignedCredit) {
        this.assignedCredit = assignedCredit;
    }

    @Override
    public HalDataOutputType displayType() {
        return type;
    }

    @Override
    public boolean include(HalDataOutputType type, String propertyName) {
        if (type != HalDataOutputType.FULL && HAL_FULL_ONLY_FIELDS.contains(propertyName)) {
            return false;
        }

        return true;
    }
}
