package me.ronygomes.ums.api.model;

import jakarta.persistence.*;

import java.util.Date;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "course_enrollments")
public class Enrollment extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_enrollment_seq")
    private Long id;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Course course;

    @Temporal(TemporalType.TIMESTAMP)
    private Date enrollmentDate;

    @Enumerated(STRING)
    private EnrollmentStatus status;

    @Enumerated(STRING)
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

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
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
