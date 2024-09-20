package me.ronygomes.ums.api.repository;

import jakarta.validation.ConstraintViolation;
import me.ronygomes.ums.api.model.Department;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionSystemException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static me.ronygomes.ums.api.testHelper.DataHelper.validPersistableDepartment1;
import static me.ronygomes.ums.api.testHelper.TestHelper.extractConstraintViolation;

@SpringBootTest
@ActiveProfiles("database-test")
@Testcontainers(disabledWithoutDocker = true)
public class DepartmentRepositoryTest {

    private static final Map<String, String> PRE_POPULATED_DEPARTMENT = new HashMap<>();

    static {
        PRE_POPULATED_DEPARTMENT.put("CE", "Civil Engineering");
        PRE_POPULATED_DEPARTMENT.put("CSE", "Computer Science & Engineering");
        PRE_POPULATED_DEPARTMENT.put("EEE", "Electrical & Electronic Engineering");
        PRE_POPULATED_DEPARTMENT.put("MPE", "Mechanical and Production Engineering");
        PRE_POPULATED_DEPARTMENT.put("TE", "Textile Engineering");
        PRE_POPULATED_DEPARTMENT.put("A&S", "Arts and Sciences");
        PRE_POPULATED_DEPARTMENT.put("ARCH", "Architecture");
        PRE_POPULATED_DEPARTMENT.put("SoB", "School of Business");
    }

    @Autowired
    private DepartmentRepository repository;

    @Test
    void testDepartmentRepositoryIsNotNull() {
        Assertions.assertNotNull(repository);
    }

    @Test
    void testInitScriptDataExists() {
        List<Department> prePopulatedDepartments = repository.findAll();
        Assertions.assertEquals(8, prePopulatedDepartments.size());

        prePopulatedDepartments.forEach(department -> {
            Assertions.assertTrue(PRE_POPULATED_DEPARTMENT.containsKey(department.getCode()));
            Assertions.assertEquals(PRE_POPULATED_DEPARTMENT.get(department.getCode()), department.getName());
        });
    }

    @Test
    void testCanCreateNewDepartment() {
        Department d = validPersistableDepartment1();
        repository.save(d);

        List<Department> allDepartments = repository.findAll();
        Assertions.assertEquals(9, allDepartments.size());

        Optional<Department> dbDepartmentOpt = repository.findById(d.getId());
        Assertions.assertTrue(dbDepartmentOpt.isPresent());

        Department dbDepartment = dbDepartmentOpt.get();
        Assertions.assertEquals(d.getId(), dbDepartment.getId());
        Assertions.assertEquals(d.getName(), dbDepartment.getName());
        Assertions.assertEquals(d.getCode(), dbDepartment.getCode());
        Assertions.assertEquals(d.getUuid(), dbDepartment.getUuid());

        dbDepartment.setCode(dbDepartment.getCode() + " Updated");
        dbDepartment.setName(dbDepartment.getName() + " Updated");

        Optional<Department> dbDepartmentOpt2 = repository.findById(d.getId());
        Assertions.assertTrue(dbDepartmentOpt2.isPresent());
        Department dbDepartment2 = dbDepartmentOpt.get();
        Assertions.assertEquals(d.getId(), dbDepartment2.getId());
        Assertions.assertEquals(d.getName() + " Updated", dbDepartment2.getName());
        Assertions.assertEquals(d.getCode() + " Updated", dbDepartment2.getCode());
        Assertions.assertEquals(d.getUuid(), dbDepartment2.getUuid());

        repository.delete(dbDepartment);

        allDepartments = repository.findAll();
        Assertions.assertEquals(8, allDepartments.size());
    }

    @Test
    void testDepartmentCodeFieldConstraints() {
        Department nullTest = validPersistableDepartment1();
        nullTest.setCode(null);

        Throwable exceptionNullTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullTest));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exceptionNullTest);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("code", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Department minLengthTest = validPersistableDepartment1();
        minLengthTest.setCode("");

        Throwable exceptionMinLengthTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(minLengthTest));
        Set<ConstraintViolation<?>> minLengthViolations = extractConstraintViolation(exceptionMinLengthTest);
        Assertions.assertEquals(1, minLengthViolations.size());
        minLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 10", v.getMessage());
            Assertions.assertEquals("code", v.getPropertyPath().toString());
            Assertions.assertEquals("", v.getInvalidValue());
        });

        Department maxLengthTest = validPersistableDepartment1();
        maxLengthTest.setCode("12345678901");

        Throwable exceptionMaxLengthTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxLengthTest));
        Set<ConstraintViolation<?>> maxLengthViolations = extractConstraintViolation(exceptionMaxLengthTest);
        Assertions.assertEquals(1, maxLengthViolations.size());
        maxLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 10", v.getMessage());
            Assertions.assertEquals("code", v.getPropertyPath().toString());
            Assertions.assertEquals("12345678901", v.getInvalidValue());
        });
    }

    @Test
    void testDepartmentNameFieldConstraints() {
        Department nullTest = validPersistableDepartment1();
        nullTest.setName(null);

        Throwable exceptionNullTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullTest));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exceptionNullTest);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("name", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Department minLengthTest = validPersistableDepartment1();
        minLengthTest.setName("");

        Throwable exceptionMinLengthTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(minLengthTest));
        Set<ConstraintViolation<?>> minLengthViolations = extractConstraintViolation(exceptionMinLengthTest);
        Assertions.assertEquals(1, minLengthViolations.size());
        minLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 100", v.getMessage());
            Assertions.assertEquals("name", v.getPropertyPath().toString());
            Assertions.assertEquals("", v.getInvalidValue());
        });

        Department maxLengthTest = validPersistableDepartment1();
        maxLengthTest.setName("#".repeat(101));

        Throwable exceptionMaxLengthTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxLengthTest));
        Set<ConstraintViolation<?>> maxLengthViolations = extractConstraintViolation(exceptionMaxLengthTest);
        Assertions.assertEquals(1, maxLengthViolations.size());
        maxLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 100", v.getMessage());
            Assertions.assertEquals("name", v.getPropertyPath().toString());
            Assertions.assertEquals("#".repeat(101), v.getInvalidValue());
        });
    }

    @Test
    void testFindByCode() {
        Optional<Department> cseOpt = repository.findByCode("CSE");
        Assertions.assertTrue(cseOpt.isPresent());

        Department cseDept = cseOpt.get();
        Assertions.assertEquals("CSE", cseDept.getCode());
        Assertions.assertEquals(PRE_POPULATED_DEPARTMENT.get("CSE"), cseDept.getName());
        Assertions.assertNotNull(cseDept.getUuid());

        Optional<Department> invalidDeptOpt = repository.findByCode("INVALID");
        Assertions.assertFalse(invalidDeptOpt.isPresent());
    }
}
