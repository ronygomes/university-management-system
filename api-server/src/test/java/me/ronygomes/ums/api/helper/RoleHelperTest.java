package me.ronygomes.ums.api.helper;

import me.ronygomes.ums.api.model.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RoleHelperTest {

    @Test
    void testRoleLength() {
        Assertions.assertEquals(3, Role.values().length);
    }

    @Test
    void testConvert() {

        Assertions.assertEquals("ROLE_ADMIN", RoleHelper.validateAndConvert("admin").getAuthority());
        Assertions.assertEquals("ROLE_TEACHER", RoleHelper.validateAndConvert("teacher").getAuthority());
        Assertions.assertEquals("ROLE_STUDENT", RoleHelper.validateAndConvert("student").getAuthority());

        Assertions.assertEquals("ROLE_ADMIN", RoleHelper.validateAndConvert("ADMIN").getAuthority());
        Assertions.assertEquals("ROLE_TEACHER", RoleHelper.validateAndConvert("TEACHER").getAuthority());
        Assertions.assertEquals("ROLE_STUDENT", RoleHelper.validateAndConvert("STUDENT").getAuthority());

        Assertions.assertEquals("ROLE_ADMIN", RoleHelper.validateAndConvert("Admin").getAuthority());
        Assertions.assertEquals("ROLE_TEACHER", RoleHelper.validateAndConvert("Teacher").getAuthority());
        Assertions.assertEquals("ROLE_STUDENT", RoleHelper.validateAndConvert("Student").getAuthority());

        Assertions.assertEquals("ROLE_ADMIN", RoleHelper.validateAndConvert("adMin").getAuthority());
        Assertions.assertEquals("ROLE_TEACHER", RoleHelper.validateAndConvert("teaCher").getAuthority());
        Assertions.assertEquals("ROLE_STUDENT", RoleHelper.validateAndConvert("stuDent").getAuthority());

        IllegalArgumentException ex1 = Assertions.assertThrows(IllegalArgumentException.class, () -> RoleHelper.validateAndConvert("Staff"));
        Assertions.assertEquals("No enum constant me.ronygomes.ums.api.model.Role.STAFF", ex1.getMessage());

        IllegalArgumentException ex2 = Assertions.assertThrows(IllegalArgumentException.class, () -> RoleHelper.validateAndConvert(" admin"));
        Assertions.assertEquals("No enum constant me.ronygomes.ums.api.model.Role. ADMIN", ex2.getMessage());

        IllegalArgumentException ex3 = Assertions.assertThrows(IllegalArgumentException.class, () -> RoleHelper.validateAndConvert(""));
        Assertions.assertEquals("No enum constant me.ronygomes.ums.api.model.Role.", ex3.getMessage());

        NullPointerException ex4 = Assertions.assertThrows(NullPointerException.class, () -> RoleHelper.validateAndConvert(null));
        Assertions.assertNull(ex4.getMessage());
    }
}
