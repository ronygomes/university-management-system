package me.ronygomes.ums.api.helper;

import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Designation;

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
}
