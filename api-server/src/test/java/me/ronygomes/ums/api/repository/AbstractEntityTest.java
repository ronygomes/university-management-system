package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.model.AbstractEntity;
import me.ronygomes.ums.api.model.Department;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.regex.Pattern;

@SpringBootTest
@ActiveProfiles("integration-test")
public class AbstractEntityTest {

    private static final Pattern UUID_PATTERN = Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");

    @Autowired
    private DepartmentRepository repository;

    @Test
    void testNewEntityWillHaveUniqueUuid() {
        AbstractEntity entity = new Department();
        Assertions.assertNotNull(entity.getUuid());
        Assertions.assertEquals(36, entity.getUuid().length());
        Assertions.assertTrue(UUID_PATTERN.matcher(entity.getUuid()).matches());

        AbstractEntity entityOther = new Department();
        Assertions.assertNotEquals(entity.getUuid(), entityOther.getUuid());
    }

    @Test
    void testUuidIsPersistedAndNotModifiable() {
        Department department1 = DataHelper.validPersistableDepartment();

        String generatedUuid = department1.getUuid();
        repository.save(department1);

        Optional<Department> dbDepartmentOpt = repository.findById(department1.getId());
        Assertions.assertTrue(dbDepartmentOpt.isPresent());
        Department dbDepartment = dbDepartmentOpt.get();

        Assertions.assertEquals(generatedUuid, dbDepartment.getUuid());
        dbDepartment.setUuid("ABC");
        repository.save(dbDepartment);

        Optional<Department> dbDepartmentOpt1 = repository.findById(dbDepartment.getId());
        Assertions.assertTrue(dbDepartmentOpt1.isPresent());
        Department dbDepartment1 = dbDepartmentOpt1.get();
        Assertions.assertEquals(generatedUuid, dbDepartment1.getUuid());
    }
}
