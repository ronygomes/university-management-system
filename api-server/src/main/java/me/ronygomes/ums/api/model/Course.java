package me.ronygomes.ums.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.math.BigDecimal;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "courses")
public class Course extends AbstractEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "courses_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "courses_seq")
    private Long id;

    @NotNull
    @Size(min = 1, max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(nullable = false)
    private BigDecimal credit;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false, foreignKey = @ForeignKey(name = "fk_courses_department_id"))
    private Department department;

    @NotNull
    @Enumerated(STRING)
    @Column(nullable = false, length = 30)
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "instructor_id", foreignKey = @ForeignKey(name = "fk_courses_instructor_id"))
    private Teacher instructor;

    public Course() {
        this.credit = new BigDecimal(0);
    }

    @Override
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

    public float getCredit() {
        return credit.floatValue();
    }

    public void setCredit(float credit) {
        this.credit = new BigDecimal(credit);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public Teacher getInstructor() {
        return instructor;
    }

    public void setInstructor(Teacher instructor) {
        this.instructor = instructor;
    }
}
