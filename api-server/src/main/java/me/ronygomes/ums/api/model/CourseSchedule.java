package me.ronygomes.ums.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.ronygomes.ums.api.converter.WeekOfDayListStringAttributeConverter;

import java.io.Serial;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "course_schedules")
public class CourseSchedule extends AbstractEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @SequenceGenerator(name = "course_schedules_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_schedules_seq")
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Department department;

    @NotNull
    @Enumerated(STRING)
    @Column(nullable = false, length = 30)
    private Semester semester;

    @NotNull
    @JsonIgnore
    @ManyToOne(optional = false)
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
    @Size(min = 1, max = 7)
    @Column(nullable = false, length = 60)
    @Convert(converter = WeekOfDayListStringAttributeConverter.class)
    private List<DayOfWeek> days;

    private Date startDate;

    private Date endDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean enrollmentOpen;

    @Transient
    private Long courseId;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String departmentCode;

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

    public List<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(List<DayOfWeek> days) {
        this.days = days;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isEnrollmentOpen() {
        return enrollmentOpen;
    }

    public void setEnrollmentOpen(boolean enrollmentOpen) {
        this.enrollmentOpen = enrollmentOpen;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    @JsonProperty
    public Long getCourseId() {
        return Objects.nonNull(courseId) ? courseId :
                Objects.nonNull(course) ? course.getId()
                        : null;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public void merge(CourseSchedule patchCs) {
        if (Objects.nonNull(patchCs.getSemester())) {
            setSemester(patchCs.getSemester());
        }

        if (Objects.nonNull(patchCs.getBuilding())) {
            setBuilding(patchCs.getBuilding());
        }

        if (Objects.nonNull(patchCs.getRoomNumber())) {
            setRoomNumber(patchCs.getRoomNumber());
        }

        if (Objects.nonNull(patchCs.getDays())) {
            setDays(patchCs.getDays());
        }

        if (Objects.nonNull(patchCs.getStartDate())) {
            setStartDate(patchCs.getStartDate());
        }

        if (Objects.nonNull(patchCs.getEndDate())) {
            setEndDate(patchCs.getEndDate());
        }

        if (Objects.nonNull(patchCs.getCourseId())) {
            setCourseId(patchCs.getCourseId());
        }

        if (Objects.nonNull(patchCs.getDepartmentCode())) {
            setDepartmentCode(patchCs.getDepartmentCode());
        }
    }
}
