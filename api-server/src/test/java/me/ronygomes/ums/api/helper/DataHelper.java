package me.ronygomes.ums.api.helper;

import me.ronygomes.ums.api.dto.DepartmentDto;
import me.ronygomes.ums.api.dto.TeacherInputDto;
import me.ronygomes.ums.api.model.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DataHelper {

    public static Department validPersistableDepartment1() {
        Department department = new Department();
        department.setCode("CODE-1");
        department.setName("Name-1");
        return department;
    }

    public static Department validPersistableDepartment2() {
        Department department = new Department();
        department.setCode("CODE-2");
        department.setName("Name-2");
        return department;
    }

    public static List<DepartmentDto> mockDBDepartments() {
        Department d1 = validPersistableDepartment1();
        d1.setId(1L);

        Department d2 = validPersistableDepartment2();
        d2.setId(2L);

        return Arrays.asList(new DepartmentDto(d1), new DepartmentDto(d2));
    }

    public static Designation validPersistableDesignation() {
        Designation designation = new Designation();
        designation.setTitle("Sample Title");
        return designation;
    }

    public static Teacher validPersistableTeacher1(Designation designation,
                                                   Department department) {

        Teacher t = new Teacher();
        t.setFullName("John Doe");
        t.setAddress("Somewhere 1");
        t.setEmail("john@example.com");
        t.setContactNumber("+5501349287652");
        t.setAssignedCredit(10f);
        t.setDesignation(designation);
        t.setDepartment(department);

        return t;
    }

    public static Teacher validPersistableTeacher2(Designation designation,
                                                   Department department) {

        Teacher t = new Teacher();
        t.setFullName("Jane Doe");
        t.setAddress("Somewhere 2");
        t.setEmail("jane@example.com");
        t.setContactNumber("+5501323928765");
        t.setAssignedCredit(15f);
        t.setDesignation(designation);
        t.setDepartment(department);

        return t;
    }

    public static Student validPersistableStudent1(Department department) {

        Student s = new Student();
        s.setFullName("Student 1");
        s.setAddress("Some Student Address");
        s.setEmail("student1@example.com");
        s.setContactNumber("+5501327738765");
        s.setDepartment(department);
        s.setRegistrationDate(new Date());
        s.setRegistrationNumber("2024-CSE-0001");

        List<Education> educations = new ArrayList<>();
        Education education1 = new Education();
        education1.setExamType(ExamType.SSC);
        education1.setGrade(Grade.A);
        education1.setCgpa(Grade.A.getGpa());
        education1.setCertificateFileName("ssc-certificate.pdf");
        education1.setCertificatePath("some-random-path/2024-CSE-0001/ssc-certificate.pdf");
        educations.add(education1);

        Education education2 = new Education();
        education2.setExamType(ExamType.HSC);
        education2.setGrade(Grade.A_PLUS);
        education2.setCgpa(Grade.A_PLUS.getGpa());
        education2.setCertificateFileName("hsc-certificate.pdf");
        education2.setCertificatePath("some-random-path/2024-CSE-0001/hsc-certificate.pdf");
        educations.add(education2);

        s.setEducations(educations);

        return s;
    }

    public static Student validPersistableStudent2(Department department) {

        Student s = new Student();
        s.setFullName("Student 2");
        s.setAddress("Some Student 2 Address");
        s.setEmail("student2@example.com");
        s.setContactNumber("+8801327738961");
        s.setDepartment(department);
        s.setRegistrationDate(new Date());
        s.setRegistrationNumber("2024-CSE-0002");

        List<Education> educations = new ArrayList<>();
        Education education1 = new Education();
        education1.setExamType(ExamType.SSC);
        education1.setGrade(Grade.A_PLUS);
        education1.setCgpa(Grade.A_PLUS.getGpa());
        education1.setCertificateFileName("ssc-certificate.pdf");
        education1.setCertificatePath("some-random-path/2024-CSE-0002/ssc-certificate.pdf");
        educations.add(education1);

        Education education2 = new Education();
        education2.setExamType(ExamType.HSC);
        education2.setGrade(Grade.B_PLUS);
        education2.setCgpa(Grade.B_PLUS.getGpa());
        education2.setCertificateFileName("hsc-certificate.pdf");
        education2.setCertificatePath("some-random-path/2024-CSE-0002/hsc-certificate.pdf");
        educations.add(education2);

        s.setEducations(educations);

        return s;
    }

    public static Student validPersistableStudentWithoutEducation(Department department) {
        Student s = new Student();
        s.setFullName("Student 3");
        s.setAddress("Some Student 3 Address");
        s.setEmail("student3@example.com");
        s.setContactNumber("+8801325738961");
        s.setDepartment(department);
        s.setRegistrationDate(new Date());
        s.setRegistrationNumber("2024-CSE-0003");

        return s;
    }

    public static Education educationWithGrade(Float cgpa) {
        Education e = new Education();
        e.setExamType(ExamType.SSC);
        e.setGrade(Grade.A_PLUS);
        e.setCgpa(cgpa);
        e.setCertificateFileName("ssc-certificate.pdf");
        e.setCertificatePath("some-random-path/2024-CSE-0002/ssc-certificate.pdf");

        return e;
    }

    public static Course validPersistableCourse1(Department department, Teacher teacher) {
        Course c = new Course();
        c.setTitle("CSE-101");
        c.setName("Introduction to Programming Language in Java");
        c.setCredit(3.0f);
        c.setDescription("Java Description");
        c.setDepartment(department);
        c.setSemester(Semester.FIRST_YEAR_FIRST);
        c.setInstructor(teacher);

        return c;
    }

    public static Course validPersistableCourse2(Department department, Teacher teacher) {
        Course c = new Course();
        c.setTitle("CSE-201");
        c.setName("Introduction to Algorithm");
        c.setCredit(4.0f);
        c.setDescription("Algorithm Description");
        c.setDepartment(department);
        c.setSemester(Semester.SECOND_YEAR_FIRST);
        c.setInstructor(teacher);

        return c;
    }

    public static Enrollment validPersistableEnrollment1(Student student, Course course) {
        Enrollment e = new Enrollment();
        e.setCourse(course);
        e.setStudent(student);
        e.setGrade(Grade.A);
        e.setStatus(EnrollmentStatus.ON_GOING);
        e.setEnrollmentDate(new Date());

        return e;
    }

    public static CourseSchedule validPersistableCourseSchedule1(Department department, Course course) {
        CourseSchedule cs = new CourseSchedule();
        cs.setDepartment(department);
        cs.setSemester(Semester.FIRST_YEAR_SECOND);
        cs.setCourse(course);
        cs.setBuilding(Building.BUILDING_1);
        cs.setRoomNumber("F7-102");
        cs.setDay(DayOfWeek.MONDAY);
        cs.setStartTime(Date.from(Instant.now().plus(Duration.ofDays(1))));
        cs.setEndTime(Date.from(Instant.now().plus(Duration.ofDays(30 * 3))));

        return cs;
    }

    public static CourseSchedule validPersistableCourseSchedule2(Department department, Course course) {
        CourseSchedule cs = new CourseSchedule();
        cs.setDepartment(department);
        cs.setSemester(Semester.SECOND_YEAR_FIRST);
        cs.setCourse(course);
        cs.setBuilding(Building.BUILDING_2);
        cs.setRoomNumber("F7-202");
        cs.setDay(DayOfWeek.TUESDAY);
        cs.setStartTime(Date.from(Instant.now().plus(Duration.ofDays(10))));
        cs.setEndTime(Date.from(Instant.now().plus(Duration.ofDays(20))));

        return cs;
    }

    public static TeacherInputDto validTeacherInputDto() {
        var teacherDto = new TeacherInputDto();
        teacherDto.setFullName("abc");
        teacherDto.setAddress("Address");
        teacherDto.setEmail("abc@def.com");
        teacherDto.setContactNumber("+1111111111111");
        teacherDto.setAssignedCredit(0);
        teacherDto.setTitle("abc");
        teacherDto.setDepartmentCode("CSE");

        return teacherDto;
    }
}
