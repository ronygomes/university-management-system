package me.ronygomes.ums.api.model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "course_schedules")
public class CourseSchedule extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_schedule_seq")
    private Long id;

    @ManyToOne
    private Department department;

    @Enumerated(STRING)
    private Semester semester;

    @OneToOne
    private Course course;

    @Enumerated(STRING)
    private Building building;

    @Column(nullable = false, length = 100)
    private String roomNumber;

    @Enumerated(STRING)
    private DayOfWeek day;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalTime startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalTime endTime;

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

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
