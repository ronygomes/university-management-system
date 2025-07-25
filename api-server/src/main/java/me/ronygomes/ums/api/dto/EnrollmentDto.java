package me.ronygomes.ums.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import me.ronygomes.ums.api.model.Enrollment;
import me.ronygomes.ums.api.model.EnrollmentStatus;
import me.ronygomes.ums.api.model.Grade;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class EnrollmentDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull
    private Long studentId;

    @NotNull
    private Long courseScheduleId;

    @NotNull
    @PastOrPresent
    private Date enrollmentDate;

    @NotNull
    private EnrollmentStatus status;

    private Grade grade;

    public EnrollmentDto() {
    }

    public EnrollmentDto(Enrollment enrollment) {
        this.id = enrollment.getId();
        this.studentId = enrollment.getStudent().getId();
        this.courseScheduleId = enrollment.getCourseSchedule().getId();
        this.enrollmentDate = enrollment.getEnrollmentDate();
        this.status = enrollment.getStatus();
        this.grade = enrollment.getGrade();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseScheduleId() {
        return courseScheduleId;
    }

    public void setCourseScheduleId(Long courseScheduleId) {
        this.courseScheduleId = courseScheduleId;
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

    public Enrollment toEnrollment(Long id) {
        Enrollment e = new Enrollment();
        e.setId(id);
        e.setEnrollmentDate(getEnrollmentDate());
        e.setGrade(getGrade());
        e.setStatus(getStatus());

        return e;
    }

    public void mergeWith(Enrollment e) {
        if (Objects.isNull(getStudentId())) {
            setStudentId(e.getStudent().getId());
        }

        if (Objects.isNull(getCourseScheduleId())) {
            setCourseScheduleId(e.getCourseSchedule().getId());
        }

        if (Objects.isNull(getEnrollmentDate())) {
            setEnrollmentDate(e.getEnrollmentDate());
        }

        if (Objects.isNull(getStatus())) {
            setStatus(e.getStatus());
        }

        if (Objects.isNull(getGrade())) {
            setGrade(e.getGrade());
        }
    }
}
