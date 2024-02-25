package me.ronygomes.ums.api.model;

import jakarta.persistence.*;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "courses")
public class Course extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_seq")
    private Long id;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(nullable = false, length = 200)
    private String name;
    private float credit;

    @Column(length = 2000)
    private String description;

    @ManyToOne
    private Department department;

    @Enumerated(STRING)
    private Semester semester;

    @ManyToOne
    private Teacher instructor;

    @OneToOne
    private CourseSchedule scheduleDetail;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
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

    public CourseSchedule getScheduleDetail() {
        return scheduleDetail;
    }

    public void setScheduleDetail(CourseSchedule scheduleDetail) {
        this.scheduleDetail = scheduleDetail;
    }
}
