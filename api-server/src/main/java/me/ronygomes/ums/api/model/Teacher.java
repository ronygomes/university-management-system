package me.ronygomes.ums.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serial;

@Entity
@Table(name = "teachers")
public class Teacher extends AbstractEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String EMAIL_REGEX_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String PHONE_REGEX_PATTERN = "^\\+\\d{13}$";

    @Id
    @SequenceGenerator(name = "teachers_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "teachers_seq")
    private Long id;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(nullable = false, length = 200)
    private String fullName;

    @Size(max = 1000)
    @Column(length = 1000)
    private String address;

    @NotNull
    @Size(max = 100)
    @Pattern(regexp = EMAIL_REGEX_PATTERN, message = "invalid email format")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Size(max = 14)
    @Pattern(regexp = PHONE_REGEX_PATTERN, message = "invalid contact number format")
    @Column(length = 14)
    private String contactNumber;

    @Min(0)
    @Max(100)
    private float assignedCredit;

    @ManyToOne
    @JoinColumn(name = "designation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teachers_designation_id"))
    private Designation designation;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teachers_department_id"))
    private Department department;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Designation getDesignation() {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
