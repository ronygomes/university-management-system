package me.ronygomes.ums.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "student_educations")
public class Education implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @SequenceGenerator(name = "student_educations_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_educations_seq")
    private Long id;

    @NotNull
    @Column(nullable = false, length = 10)
    @Enumerated(STRING)
    private ExamType examType;

    @NotNull
    @Column(nullable = false, length = 10)
    @Enumerated(STRING)
    private Grade grade;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(nullable = false)
    private BigDecimal cgpa;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(nullable = false, updatable = false, length = 100)
    private String certificateFileName;

    @NotNull
    @Size(min = 1, max = 150)
    @Column(nullable = false, updatable = false, length = 150)
    private String certificatePath;

    public Education() {
        this.cgpa = new BigDecimal(0);
    }

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
        return cgpa.floatValue();
    }

    public void setCgpa(float cgpa) {
        this.cgpa = new BigDecimal(cgpa);
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
        return education.cgpa.compareTo(cgpa) == 0
                && examType == education.examType
                && grade == education.grade
                && certificateFileName.equals(education.certificateFileName)
                && certificatePath.equals(education.certificatePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(examType, grade, cgpa, certificateFileName, certificatePath);
    }

    public void merge(Education education) {
        if (Objects.nonNull(education.getExamType())) {
            setExamType(education.getExamType());
        }

        if (Objects.nonNull(education.getGrade())) {
            setGrade(education.getGrade());
        }

        if (Objects.nonNull(education.cgpa)) {
            setCgpa(education.cgpa.floatValue());
        }

        if (Objects.nonNull(education.getCertificatePath())) {
            setCertificatePath(education.getCertificatePath());
        }

        if (Objects.nonNull(education.getCertificateFileName())) {
            setCertificateFileName(education.getCertificateFileName());
        }
    }
}
