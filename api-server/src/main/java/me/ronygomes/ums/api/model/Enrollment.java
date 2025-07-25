package me.ronygomes.ums.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.io.Serial;
import java.util.Date;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "course_enrollments")
public class Enrollment extends AbstractEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "course_enrollments_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_enrollments_seq")
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    private Student student;

    @NotNull
    @ManyToOne(optional = false)
    private CourseSchedule courseSchedule;

    @NotNull
    @PastOrPresent
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date enrollmentDate;

    @NotNull
    @Enumerated(STRING)
    @Column(nullable = false, length = 10)
    private EnrollmentStatus status;

    @Enumerated(STRING)
    @Column(length = 10)
    private Grade grade;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public CourseSchedule getCourseSchedule() {
        return courseSchedule;
    }

    public void setCourseSchedule(CourseSchedule courseSchedule) {
        this.courseSchedule = courseSchedule;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }
}
