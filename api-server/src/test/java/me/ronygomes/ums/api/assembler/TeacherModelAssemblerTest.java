package me.ronygomes.ums.api.assembler;

import me.ronygomes.ums.api.dto.TeacherOutputDto;
import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Designation;
import me.ronygomes.ums.api.model.Teacher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.RepresentationModel;

public class TeacherModelAssemblerTest {

    private TeacherModelAssembler assembler;

    @BeforeEach
    void setup() {
        assembler = new TeacherModelAssembler();
    }

    @Test
    void testToModel() {
        Teacher input = createMockDBTeacher();
        TeacherOutputDto output = assembler.toModel(input);
        Assertions.assertTrue(RepresentationModel.class.isAssignableFrom(output.getClass()));

        Assertions.assertEquals(input.getFullName(), output.getFullName());
        Assertions.assertEquals(input.getAddress(), output.getAddress());
        Assertions.assertEquals(input.getEmail(), output.getEmail());
        Assertions.assertEquals(input.getContactNumber(), output.getContactNumber());
        Assertions.assertEquals(input.getAssignedCredit(), output.getAssignedCredit());
        Assertions.assertEquals(input.getDesignation().getTitle(), output.getDesignation().getTitle());
        Assertions.assertNotNull(output.getDepartment().getContent());
        Assertions.assertEquals(input.getDepartment().getCode(), output.getDepartment().getContent().getCode());
        Assertions.assertEquals(input.getDepartment().getName(), output.getDepartment().getContent().getName());
    }

    private Teacher createMockDBTeacher() {
        Designation dbMockTitle = DataHelper.validPersistableDesignation();
        dbMockTitle.setId(10L);

        Department dbMockDepartment = DataHelper.validPersistableDepartment1();
        dbMockTitle.setId(57L);

        Teacher dbMockTeacher = DataHelper.validPersistableTeacher1(dbMockTitle, dbMockDepartment);
        dbMockTeacher.setId(32L);

        return dbMockTeacher;
    }
}
