package me.ronygomes.ums.api.model;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "student_educations")
public class Education implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "student_educations_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_educations_seq")
    private Long id;

    @Column(nullable = false, length = 10)
    @Enumerated(STRING)
    private ExamType examType;

    @Column(nullable = false, length = 10)
    @Enumerated(STRING)
    private Grade grade;

    private float cgpa;

    @Column(nullable = false, updatable = false, length = 100)
    private String certificateFileName;

    @Column(nullable = false, updatable = false, length = 150)
    private String certificatePath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
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
