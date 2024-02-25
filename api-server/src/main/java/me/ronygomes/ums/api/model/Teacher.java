package me.ronygomes.ums.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teachers")
public class Teacher extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "teacher_seq")
    private Long id;

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(length = 1000)
    private String address;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 14)
    private String contactNumber;

    @ManyToOne
    private Designation designation;

    @ManyToOne
    private Department department;

    private float assignedCredit;

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

    public float getAssignedCredit() {
        return assignedCredit;
    }

    public void setAssignedCredit(float assignedCredit) {
        this.assignedCredit = assignedCredit;
    }
}
