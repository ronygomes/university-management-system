package me.ronygomes.ums.api.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "student_educations")
public class Education implements Serializable {

    @Column(nullable = false, length = 10)
    @Enumerated(STRING)
    private ExamType examType;

    @Column(length = 5)
    @Enumerated(STRING)
    private Grade grade;

    private float cgpa;

    // Save as <student-id>-<exam-type>-<grade-cgpa-hash>-<file-name>,
    // Discard entry with same values
    @Column(length = 150, updatable = false)
    private String certificateFileName;

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public float getCgpa() {
        return cgpa;
    }

    public void setCgpa(float cgpa) {
        this.cgpa = cgpa;
    }

    public String getCertificateFileName() {
        return certificateFileName;
    }

    public void setCertificateFileName(String certificateFileName) {
        this.certificateFileName = certificateFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Education education = (Education) o;
        return Float.compare(education.cgpa, cgpa) == 0
                && examType == education.examType
                && grade == education.grade
                && certificateFileName.equals(education.certificateFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(examType, grade, cgpa, certificateFileName);
    }
}
