package me.ronygomes.ums.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.time.DayOfWeek;
import java.util.Date;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "course_schedules")
public class CourseSchedule extends AbstractEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "course_schedules_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_schedules_seq")
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    private Department department;

    @NotNull
    @Enumerated(STRING)
    @Column(nullable = false, length = 30)
    private Semester semester;

    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false, unique = true)
    private Course course;

    @NotNull
    @Enumerated(STRING)
    @Column(nullable = false, length = 30)
    private Building building;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String roomNumber;

    @NotNull
    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private DayOfWeek day;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
