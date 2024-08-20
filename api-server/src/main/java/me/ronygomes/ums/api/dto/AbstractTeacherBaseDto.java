package me.ronygomes.ums.api.dto;

import jakarta.validation.constraints.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;

import static me.ronygomes.ums.api.model.AbstractEntity.EMAIL_REGEX_PATTERN;
import static me.ronygomes.ums.api.model.AbstractEntity.PHONE_REGEX_PATTERN;

public abstract class AbstractTeacherBaseDto<T extends RepresentationModel<? extends T>>
        extends RepresentationModel<T> implements Serializable {

    @NotNull
    @Size(min = 1, max = 200)
    private String fullName;

    @Size(max = 1000)
    private String address;

    @NotNull
    @Size(min = 5, max = 100)
    @Pattern(regexp = EMAIL_REGEX_PATTERN, message = "invalid email format")
    private String email;

    @Pattern(regexp = PHONE_REGEX_PATTERN, message = "invalid contact number format")
    private String contactNumber;

    @Min(0)
    @Max(100)
    private float assignedCredit;

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

    public float getAssignedCredit() {
        return assignedCredit;
    }

    public void setAssignedCredit(float assignedCredit) {
        this.assignedCredit = assignedCredit;
    }
}
