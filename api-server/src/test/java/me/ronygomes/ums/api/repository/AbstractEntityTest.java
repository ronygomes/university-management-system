package me.ronygomes.ums.api.repository;

import jakarta.validation.ConstraintViolation;
import me.ronygomes.ums.api.testHelper.DataHelper;
import me.ronygomes.ums.api.model.AbstractEntity;
import me.ronygomes.ums.api.model.Department;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionSystemException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static me.ronygomes.ums.api.testHelper.TestHelper.extractConstraintViolation;

@SpringBootTest
@ActiveProfiles("database-test")
@Testcontainers(disabledWithoutDocker = true)
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
        Department department1 = DataHelper.validPersistableDepartment1();

        String generatedUuid = department1.getUuid();
        repository.save(department1);

        Optional<Department> dbDepartmentOpt = repository.findById(department1.getId());
        Assertions.assertTrue(dbDepartmentOpt.isPresent());
        Department dbDepartment = dbDepartmentOpt.get();

        String newGenerateUuid = UUID.randomUUID().toString();
        Assertions.assertEquals(generatedUuid, dbDepartment.getUuid());
        dbDepartment.setUuid(newGenerateUuid);
        repository.save(dbDepartment);

        Optional<Department> dbDepartmentOpt1 = repository.findById(dbDepartment.getId());
        Assertions.assertTrue(dbDepartmentOpt1.isPresent());
        Department dbDepartment1 = dbDepartmentOpt1.get();
        Assertions.assertEquals(generatedUuid, dbDepartment1.getUuid());

        repository.delete(department1);
    }

    @Test
    void testRequiresValidUuid() {
        Department department1 = DataHelper.validPersistableDepartment1();
        department1.setUuid(null);

        Throwable exceptionNullTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(department1));
        Set<ConstraintViolation<?>> nullTestViolations = extractConstraintViolation(exceptionNullTest);
        Assertions.assertEquals(1, nullTestViolations.size());
        nullTestViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("uuid", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Department department2 = DataHelper.validPersistableDepartment1();
        department2.setUuid("ABC");

        Throwable invalidPatternTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(department2));
        Set<ConstraintViolation<?>> invalidPatternViolations = extractConstraintViolation(invalidPatternTest);
        Assertions.assertEquals(1, invalidPatternViolations.size());
        invalidPatternViolations.forEach(v -> {
            Assertions.assertEquals("invalid uuid", v.getMessage());
            Assertions.assertEquals("uuid", v.getPropertyPath().toString());
            Assertions.assertEquals("ABC", v.getInvalidValue());
        });
    }
}
