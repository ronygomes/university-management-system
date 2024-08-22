package me.ronygomes.ums.api.dto;

import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Designation;
import me.ronygomes.ums.api.model.Teacher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TeacherPatchDtoTest {

    private Teacher mockDBTeacher;
    private TeacherPatchInputDto dto;

    @BeforeEach
    void setup() {
        mockDBTeacher = createMockTeacher();
        dto = new TeacherPatchInputDto();
    }

    @Test
    void testTeacherPatchDto_fullName() {
        dto.setFullName("Updated");
        TeacherDto idto = dto.toInputDto(mockDBTeacher);
        assertEqualsExcept(mockDBTeacher, idto, "fullName");
        Assertions.assertEquals("Updated", idto.getFullName());
    }

    @Test
    void testTeacherPatchDto_address() {
        dto.setAddress("Updated");
        TeacherDto idto = dto.toInputDto(mockDBTeacher);
        assertEqualsExcept(mockDBTeacher, idto, "address");
        Assertions.assertEquals("Updated", idto.getAddress());
    }

    @Test
    void testTeacherPatchDto_email() {
        dto.setEmail("Updated");
        TeacherDto idto = dto.toInputDto(mockDBTeacher);
        assertEqualsExcept(mockDBTeacher, idto, "email");
        Assertions.assertEquals("Updated", idto.getEmail());
    }

    @Test
    void testTeacherPatchDto_contactNumber() {
        dto.setContactNumber("Updated");
        TeacherDto idto = dto.toInputDto(mockDBTeacher);
        assertEqualsExcept(mockDBTeacher, idto, "contactNumber");
        Assertions.assertEquals("Updated", idto.getContactNumber());
    }

    @Test
    void testTeacherPatchDto_assignedCredit() {
        dto.setAssignedCredit(5.0f);
        TeacherDto idto = dto.toInputDto(mockDBTeacher);
        assertEqualsExcept(mockDBTeacher, idto, "assignedCredit");
        Assertions.assertEquals(5.0f, idto.getAssignedCredit());
    }

    @Test
    void testTeacherPatchDto_departmentCode() {
        dto.setDepartmentCode("RD");

        TeacherDto idto = dto.toInputDto(mockDBTeacher);
        assertEqualsExcept(mockDBTeacher, idto, "departmentCode");
        Assertions.assertEquals("RD", idto.getDepartmentCode());
    }

    @Test
    void testTeacherPatchDto_title() {
        dto.setTitle("Abc");

        TeacherDto idto = dto.toInputDto(mockDBTeacher);
        assertEqualsExcept(mockDBTeacher, idto, "title");
        Assertions.assertEquals("Abc", idto.getTitle());
    }

    private void assertEqualsExcept(Teacher original, TeacherDto updated, String field) {
        if (!"fullName".equals(field)) {
            Assertions.assertEquals(original.getFullName(), updated.getFullName());
        }

        if (!"address".equals(field)) {
            Assertions.assertEquals(original.getAddress(), updated.getAddress());
        }

        if (!"email".equals(field)) {
            Assertions.assertEquals(original.getEmail(), updated.getEmail());
        }

        if (!"contactNumber".equals(field)) {
            Assertions.assertEquals(original.getContactNumber(), updated.getContactNumber());
        }

        if (!"assignedCredit".equals(field)) {
            Assertions.assertEquals(original.getAssignedCredit(), updated.getAssignedCredit());
        }

        if (!"title".equals(field)) {
            Assertions.assertEquals(original.getDesignation().getTitle(), updated.getTitle());
        }

        if (!"departmentCode".equals(field)) {
            Assertions.assertEquals(original.getDepartment().getCode(), updated.getDepartmentCode());
        }
    }

    private Teacher createMockTeacher() {
        Designation designation = DataHelper.validPersistableDesignation();
        designation.setId(50L);

        Department department = DataHelper.validPersistableDepartment1();
        department.setId(23L);

        Teacher teacher = DataHelper.validPersistableTeacher1(designation, department);
        teacher.setId(45L);

        return teacher;
    }
}
