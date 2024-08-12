package me.ronygomes.ums.api.repository;

import jakarta.validation.ConstraintViolation;
import me.ronygomes.ums.api.model.Designation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionSystemException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static me.ronygomes.ums.api.helper.DataHelper.validPersistableDesignation;
import static me.ronygomes.ums.api.helper.TestHelper.extractConstraintViolation;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers(disabledWithoutDocker = true)
public class DesignationRepositoryTest {

    private static final List<String> PRE_POPULATED_DESIGNATION = Arrays.asList("Assistant Professor",
            "Associate Professor", "Lecturer", "Professor");

    @Autowired
    private DesignationRepository repository;

    @Test
    void testDesignationRepositoryIsNotNull() {
        Assertions.assertNotNull(repository);
    }

    @Test
    void testInitScriptDataExists() {
        List<Designation> prePopulatedDesignations = repository.findAll();
        Assertions.assertEquals(4, prePopulatedDesignations.size());

        List<String> prePopulatedDesignationTitles = prePopulatedDesignations
                .stream()
                .map(Designation::getTitle)
                .sorted()
                .toList();

        Assertions.assertEquals(PRE_POPULATED_DESIGNATION, prePopulatedDesignationTitles);
    }

    @Test
    void testCreateDesignation() {
        Designation d = validPersistableDesignation();

        Assertions.assertNull(d.getId());
        repository.save(d);
        Assertions.assertNotNull(d.getId());
        Assertions.assertEquals(5, repository.findAll().size());

        Optional<Designation> dbStoredOpt = repository.findById(d.getId());
        Assertions.assertTrue(dbStoredOpt.isPresent());
        Assertions.assertEquals(d.getTitle(), dbStoredOpt.get().getTitle());

        repository.delete(dbStoredOpt.get());
        Assertions.assertEquals(4, repository.findAll().size());
    }

    @Test
    void testUpdateDesignation() {
        Designation d = validPersistableDesignation();

        Assertions.assertNull(d.getId());
        repository.save(d);
        Assertions.assertNotNull(d.getId());
        Assertions.assertEquals(5, repository.findAll().size());

        Optional<Designation> dbStoredOpt1 = repository.findById(d.getId());
        Assertions.assertTrue(dbStoredOpt1.isPresent());
        Designation dbDesignation = dbStoredOpt1.get();
        dbDesignation.setTitle("Updated");
        repository.save(dbDesignation);

        Optional<Designation> dbStoredOpt2 = repository.findById(dbDesignation.getId());
        Assertions.assertTrue(dbStoredOpt2.isPresent());
        Assertions.assertEquals("Updated", dbStoredOpt2.get().getTitle());
        Assertions.assertEquals(d.getId(), dbStoredOpt2.get().getId());

        repository.delete(dbStoredOpt2.get());
        Assertions.assertEquals(4, repository.findAll().size());
    }

    @Test
    void testUniqueDesignationTitle() {
        Designation d1 = validPersistableDesignation();
        Designation d2 = validPersistableDesignation();

        repository.save(d1);
        Assertions.assertEquals(5, repository.findAll().size());

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(d2));
        Assertions.assertEquals(5, repository.findAll().size());

        repository.delete(d1);
        Assertions.assertEquals(4, repository.findAll().size());
    }

    @Test
    void testDesignationTitleFieldConstraints() {
        Designation nullTest = validPersistableDesignation();
        nullTest.setTitle(null);

        Throwable exceptionNullTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(nullTest));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exceptionNullTest);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("title", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Designation minLengthTest = validPersistableDesignation();
        minLengthTest.setTitle("");

        Throwable exceptionMinLengthTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(minLengthTest));
        Set<ConstraintViolation<?>> minLengthViolations = extractConstraintViolation(exceptionMinLengthTest);
        Assertions.assertEquals(1, minLengthViolations.size());
        minLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 100", v.getMessage());
            Assertions.assertEquals("title", v.getPropertyPath().toString());
            Assertions.assertEquals("", v.getInvalidValue());
        });

        Designation maxLengthTest = validPersistableDesignation();
        maxLengthTest.setTitle("#".repeat(101));

        Throwable exceptionMaxLengthTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(maxLengthTest));
        Set<ConstraintViolation<?>> maxLengthViolations = extractConstraintViolation(exceptionMaxLengthTest);
        Assertions.assertEquals(1, maxLengthViolations.size());
        maxLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 100", v.getMessage());
            Assertions.assertEquals("title", v.getPropertyPath().toString());
            Assertions.assertEquals("#".repeat(101), v.getInvalidValue());
        });
    }

    @Test
    void testFindByTitle() {
        Designation designation1 = repository.findByTitle("Lecturer").orElseThrow();
        Assertions.assertEquals("Lecturer", designation1.getTitle());

        Optional<Designation> designation2Opt = repository.findByTitle("NonExistent");
        Assertions.assertTrue(designation2Opt.isEmpty());
    }

    @Test
    void testFindAllByOrderByTitleAsc() {
        List<String> designations = repository.findAllByOrderByTitleAsc()
                .stream()
                .map(Designation::getTitle)
                .toList();

        Assertions.assertEquals(PRE_POPULATED_DESIGNATION, designations);
    }
}
