package me.ronygomes.ums.api.helper;

import me.ronygomes.ums.api.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataHelper {

    public static Department validPersistableDepartment() {
        Department department = new Department();
        department.setCode("CODE-1");
        department.setName("Name-1");
        return department;
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
}
