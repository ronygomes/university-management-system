package me.ronygomes.ums.api.helper;

import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Designation;
import me.ronygomes.ums.api.model.Teacher;

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
}
