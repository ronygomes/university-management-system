package me.ronygomes.ums.api.repository;

import jakarta.validation.ConstraintViolation;
import me.ronygomes.ums.api.testHelper.DataHelper;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Designation;
import me.ronygomes.ums.api.model.Teacher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionSystemException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.Set;

import static me.ronygomes.ums.api.testHelper.DataHelper.validPersistableDepartment1;
import static me.ronygomes.ums.api.testHelper.TestHelper.extractConstraintViolation;

@SpringBootTest
@ActiveProfiles("database-test")
@Testcontainers(disabledWithoutDocker = true)
public class TeacherRepositoryTest {

    @Autowired
    private TeacherRepository repository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Test
    void testCanInsertTeacher() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();
        Designation designation = designationRepository.findAll().get(0);

        Teacher teacher = DataHelper.validPersistableTeacher1(designation, department);

        Assertions.assertEquals(0, repository.findAll().size());
        repository.save(teacher);
        Assertions.assertEquals(1, repository.findAll().size());

        Teacher dBTeacher = repository.findById(teacher.getId()).orElseThrow();
        Assertions.assertEquals(teacher, dBTeacher);

        Assertions.assertEquals(teacher.getId(), dBTeacher.getId());
        Assertions.assertEquals(teacher.getFullName(), dBTeacher.getFullName());
        Assertions.assertEquals(teacher.getAddress(), dBTeacher.getAddress());
        Assertions.assertEquals(teacher.getEmail(), dBTeacher.getEmail());
        Assertions.assertEquals(teacher.getContactNumber(), dBTeacher.getContactNumber());
        Assertions.assertEquals(teacher.getAssignedCredit(), dBTeacher.getAssignedCredit());

        Assertions.assertEquals(department, dBTeacher.getDepartment());
        Assertions.assertEquals(department.getId(), dBTeacher.getDepartment().getId());

        Assertions.assertEquals(designation, dBTeacher.getDesignation());
        Assertions.assertEquals(designation.getId(), dBTeacher.getDesignation().getId());

        Assertions.assertEquals(8, departmentRepository.findAll().size());
        Assertions.assertEquals(4, designationRepository.findAll().size());
        repository.delete(teacher);
        Assertions.assertEquals(0, repository.findAll().size());
        Assertions.assertEquals(8, departmentRepository.findAll().size());
        Assertions.assertEquals(4, designationRepository.findAll().size());
    }

    @Test
    void testSavingTeacherDoesNotUpdateDepartment() {
        Designation designation = designationRepository.findAll().get(0);
        Department departmentExisting = departmentRepository.findByCode("CSE").orElseThrow();

        Teacher teacher = DataHelper.validPersistableTeacher1(designation, departmentExisting);
        // Won't be updated as cascade=None. It is a detached object i.e. outside transaction
        departmentExisting.setName("Random Name");

        Assertions.assertEquals(0, repository.findAll().size());

        // @ManyToOne(cascade = CascadeType.PERSIST) means  while PERSISTing Teacher, it will try to merge (as existing) Department.
        // But if you update the ORM mapping it will throw exception because department is detached object
        // Use departmentRepository.save(department) if you need to update department
        repository.save(teacher);
        Assertions.assertEquals(1, repository.findAll().size());
        Assertions.assertEquals("Computer Science & Engineering",
                departmentRepository.findByCode("CSE").orElseThrow().getName());

        repository.delete(teacher);
        Assertions.assertEquals(0, repository.findAll().size());
    }

    @Test
    void testMustUseExistingDepartment() {
        Department departmentNew = validPersistableDepartment1();
        Designation designation = designationRepository.findAll().get(0);

        Teacher teacher = DataHelper.validPersistableTeacher1(designation, departmentNew);
        Assertions.assertEquals(0, repository.findAll().size());
        // For @ManyToOne(cascade = CascadeType.PERSIST) it will add new department in database
        Assertions.assertThrows(InvalidDataAccessApiUsageException.class, () -> repository.save(teacher));

        Assertions.assertEquals(0, repository.findAll().size());
        Assertions.assertTrue(departmentRepository.findByCode("CODE-1").isEmpty());
    }

    @Test
    void testMustUseExistingDesignation() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();
        Designation designationNew = DataHelper.validPersistableDesignation();

        Teacher teacher = DataHelper.validPersistableTeacher1(designationNew, department);
        Assertions.assertEquals(0, repository.findAll().size());
        // For @ManyToOne(cascade = CascadeType.PERSIST) it will add new designation in database
        Assertions.assertThrows(InvalidDataAccessApiUsageException.class, () -> repository.save(teacher));

        Assertions.assertEquals(0, repository.findAll().size());
        Assertions.assertEquals(4, designationRepository.findAll().size());
    }

    @Test
    void testSavingTeacherDoesNotUpdateDesignation() {
        Designation designationExisting = designationRepository.findByTitle("Lecturer").orElseThrow();
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Teacher teacher = DataHelper.validPersistableTeacher1(designationExisting, department);
        // Won't be updated as cascade=None. It is a detached object i.e. outside transaction
        designationExisting.setTitle("Random Name");

        Assertions.assertEquals(0, repository.findAll().size());

        repository.save(teacher);
        Assertions.assertEquals(1, repository.findAll().size());
        Assertions.assertEquals("Lecturer", designationRepository.findByTitle("Lecturer").orElseThrow().getTitle());

        repository.delete(teacher);
        Assertions.assertEquals(0, repository.findAll().size());
    }

    @Test
    void testTeacherFullNameFieldConstraints() {
        Designation designation = designationRepository.findByTitle("Lecturer").orElseThrow();
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Teacher teacherNullFullName = DataHelper.validPersistableTeacher1(designation, department);
        teacherNullFullName.setFullName(null);
        Throwable exceptionNullTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherNullFullName));
        Set<ConstraintViolation<?>> nullViolations = extractConstraintViolation(exceptionNullTest);
        Assertions.assertEquals(1, nullViolations.size());
        nullViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("fullName", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Teacher teacherZeroLengthFullName = DataHelper.validPersistableTeacher1(designation, department);
        teacherZeroLengthFullName.setFullName("");
        Throwable exceptionMinLengthTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherZeroLengthFullName));
        Set<ConstraintViolation<?>> exceptionMinLengthViolations = extractConstraintViolation(exceptionMinLengthTest);
        Assertions.assertEquals(1, exceptionMinLengthViolations.size());
        exceptionMinLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 200", v.getMessage());
            Assertions.assertEquals("fullName", v.getPropertyPath().toString());
            Assertions.assertEquals("", v.getInvalidValue());
        });

        Teacher teacherMaxLengthFullName = DataHelper.validPersistableTeacher1(designation, department);
        teacherMaxLengthFullName.setFullName("A".repeat(201));
        Throwable exceptionMaxLengthTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherMaxLengthFullName));
        Set<ConstraintViolation<?>> exceptionMaxLengthViolations = extractConstraintViolation(exceptionMaxLengthTest);
        Assertions.assertEquals(1, exceptionMaxLengthViolations.size());
        exceptionMaxLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 1 and 200", v.getMessage());
            Assertions.assertEquals("fullName", v.getPropertyPath().toString());
            Assertions.assertEquals("A".repeat(201), v.getInvalidValue());
        });
    }

    @Test
    void testTeacherAddressFieldConstraints() {
        Designation designation = designationRepository.findByTitle("Lecturer").orElseThrow();
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Teacher teacherNullAddress = DataHelper.validPersistableTeacher1(designation, department);
        teacherNullAddress.setAddress(null);
        Assertions.assertDoesNotThrow(() -> repository.save(teacherNullAddress));
        repository.deleteById(teacherNullAddress.getId());

        Teacher teacherMaxLengthAddress = DataHelper.validPersistableTeacher1(designation, department);
        teacherMaxLengthAddress.setAddress("A".repeat(1001));
        Throwable exceptionMaxLengthTest = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherMaxLengthAddress));
        Set<ConstraintViolation<?>> exceptionMaxLengthViolations = extractConstraintViolation(exceptionMaxLengthTest);
        Assertions.assertEquals(1, exceptionMaxLengthViolations.size());
        exceptionMaxLengthViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 0 and 1000", v.getMessage());
            Assertions.assertEquals("address", v.getPropertyPath().toString());
            Assertions.assertEquals("A".repeat(1001), v.getInvalidValue());
        });
    }

    @Test
    void testTeacherEmailFieldConstraints() {
        Designation designation = designationRepository.findByTitle("Lecturer").orElseThrow();
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Teacher teacherNullEmail = DataHelper.validPersistableTeacher1(designation, department);
        teacherNullEmail.setEmail(null);
        Throwable exceptionNullEmail = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherNullEmail));
        Set<ConstraintViolation<?>> exceptionNullEmailViolations = extractConstraintViolation(exceptionNullEmail);
        Assertions.assertEquals(1, exceptionNullEmailViolations.size());
        exceptionNullEmailViolations.forEach(v -> {
            Assertions.assertEquals("must not be null", v.getMessage());
            Assertions.assertEquals("email", v.getPropertyPath().toString());
            Assertions.assertNull(v.getInvalidValue());
        });

        Teacher teacherUnique1 = DataHelper.validPersistableTeacher1(designation, department);
        Teacher teacherUnique2 = DataHelper.validPersistableTeacher2(designation, department);
        teacherUnique2.setEmail(teacherUnique1.getEmail());

        repository.save(teacherUnique1);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(teacherUnique2));

        Assertions.assertNotNull(teacherUnique2.getId());
        Assertions.assertTrue(repository.findById(teacherUnique2.getId()).isEmpty());

        repository.deleteById(teacherUnique1.getId());

        Teacher teacherMaxLengthEmail = DataHelper.validPersistableTeacher1(designation, department);
        teacherMaxLengthEmail.setEmail("a".repeat(91) + "@gmail.com");
        Throwable exceptionMaxLengthEmail = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherMaxLengthEmail));
        Set<ConstraintViolation<?>> exceptionMaxLengthEmailViolations = extractConstraintViolation(exceptionMaxLengthEmail);
        Assertions.assertEquals(1, exceptionMaxLengthEmailViolations.size());
        exceptionMaxLengthEmailViolations.forEach(v -> {
            Assertions.assertEquals("size must be between 5 and 100", v.getMessage());
            Assertions.assertEquals("email", v.getPropertyPath().toString());
            Assertions.assertEquals(101, v.getInvalidValue().toString().length());
        });

        Teacher teacherMinLengthEmail = DataHelper.validPersistableTeacher1(designation, department);
        teacherMinLengthEmail.setEmail("a@b");
        Throwable exceptionMinLengthEmail = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherMinLengthEmail));
        Set<ConstraintViolation<?>> exceptionMinLengthEmailViolations = extractConstraintViolation(exceptionMinLengthEmail);
        Assertions.assertEquals(2, exceptionMinLengthEmailViolations.size());
        exceptionMinLengthEmailViolations.forEach(v -> {
            Assertions.assertTrue(Arrays.asList("invalid email format", "size must be between 5 and 100").contains(v.getMessage()));
            Assertions.assertEquals("email", v.getPropertyPath().toString());
            Assertions.assertEquals(3, v.getInvalidValue().toString().length());
        });

        Teacher teacherPatternEmail = DataHelper.validPersistableTeacher1(designation, department);
        teacherPatternEmail.setEmail("a".repeat(100));
        Throwable exceptionInvalidEmail = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherPatternEmail));
        Set<ConstraintViolation<?>> exceptionInvalidEmailViolations = extractConstraintViolation(exceptionInvalidEmail);
        Assertions.assertEquals(1, exceptionInvalidEmailViolations.size());
        exceptionInvalidEmailViolations.forEach(v -> {
            Assertions.assertEquals("invalid email format", v.getMessage());
            Assertions.assertEquals("email", v.getPropertyPath().toString());
            Assertions.assertEquals(100, v.getInvalidValue().toString().length());
        });
    }

    @Test
    void testTeacherContactNumberFieldConstraints() {
        Designation designation = designationRepository.findByTitle("Lecturer").orElseThrow();
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Teacher teacherNullContactNumber = DataHelper.validPersistableTeacher1(designation, department);
        teacherNullContactNumber.setContactNumber(null);
        repository.save(teacherNullContactNumber);

        Assertions.assertNull(repository.findById(teacherNullContactNumber.getId())
                .orElseThrow(IllegalStateException::new).getContactNumber());

        repository.deleteById(teacherNullContactNumber.getId());

        Teacher teacherMaxLengthContactNumber = DataHelper.validPersistableTeacher1(designation, department);
        teacherMaxLengthContactNumber.setContactNumber(teacherMaxLengthContactNumber.getContactNumber() + "2");
        Throwable exceptionMaxLengthContactNumber = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherMaxLengthContactNumber));
        Set<ConstraintViolation<?>> exceptionMaxLengthContactNumberViolations = extractConstraintViolation(exceptionMaxLengthContactNumber);
        Assertions.assertEquals(2, exceptionMaxLengthContactNumberViolations.size());
        exceptionMaxLengthContactNumberViolations.forEach(v -> {
            Assertions.assertTrue(Arrays.asList("size must be between 0 and 14", "invalid contact number format").contains(v.getMessage()));
            Assertions.assertEquals("contactNumber", v.getPropertyPath().toString());
            Assertions.assertEquals(15, v.getInvalidValue().toString().length());
        });
    }

    @Test
    void testTeacherAssignedCreditFieldConstraints() {
        Designation designation = designationRepository.findByTitle("Lecturer").orElseThrow();
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Teacher teacherAssignedCredit = DataHelper.validPersistableTeacher1(designation, department);
        teacherAssignedCredit.setAssignedCredit(-1);

        Throwable exceptionNegativeCredit = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherAssignedCredit));
        Set<ConstraintViolation<?>> assignedCreditViolations = extractConstraintViolation(exceptionNegativeCredit);
        Assertions.assertEquals(1, assignedCreditViolations.size());
        assignedCreditViolations.forEach(v -> {
            Assertions.assertEquals("must be greater than or equal to 0", v.getMessage());
            Assertions.assertEquals("assignedCredit", v.getPropertyPath().toString());
            Assertions.assertEquals(0, Float.compare(-1f, (float) v.getInvalidValue()));
        });

        Teacher teacherMaxCredit = DataHelper.validPersistableTeacher1(designation, department);
        teacherMaxCredit.setAssignedCredit(101);

        Throwable exceptionMaxCredit = Assertions.assertThrows(TransactionSystemException.class, () -> repository.save(teacherMaxCredit));
        Set<ConstraintViolation<?>> assignedMaxCreditViolations = extractConstraintViolation(exceptionMaxCredit);
        Assertions.assertEquals(1, assignedMaxCreditViolations.size());
        assignedMaxCreditViolations.forEach(v -> {
            Assertions.assertEquals("must be less than or equal to 100", v.getMessage());
            Assertions.assertEquals("assignedCredit", v.getPropertyPath().toString());
            Assertions.assertEquals(0, Float.compare(101f, (float) v.getInvalidValue()));
        });
    }

    @Test
    void testTeacherDesignationFieldConstraints() {
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Teacher teacherWithNullDesignation = DataHelper.validPersistableTeacher1(null, department);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(teacherWithNullDesignation));

        Designation d = new Designation();
        d.setId(100L);

        Teacher teacherWithNonExistentDesignation = DataHelper.validPersistableTeacher1(d, department);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(teacherWithNonExistentDesignation));
    }

    @Test
    void testTeacherDepartmentFieldConstraints() {
        Designation designation = designationRepository.findByTitle("Lecturer").orElseThrow();

        Teacher teacherWithNullDepartment = DataHelper.validPersistableTeacher1(designation, null);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(teacherWithNullDepartment));

        Department d = new Department();
        d.setId(500L);

        Teacher teacherWithNonExistentDepartment = DataHelper.validPersistableTeacher1(designation, d);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> repository.save(teacherWithNonExistentDepartment));
    }

    @Test
    void testCanUpdatedAllFields() {
        Designation designation = designationRepository.findByTitle("Lecturer").orElseThrow();
        Department department = departmentRepository.findByCode("CSE").orElseThrow();

        Assertions.assertEquals(0, repository.findAll().size());
        Teacher teacher = DataHelper.validPersistableTeacher1(designation, department);
        repository.save(teacher);
        Assertions.assertEquals(1, repository.findAll().size());

        Teacher dbTeacher = repository.findById(teacher.getId()).orElseThrow(IllegalStateException::new);
        Assertions.assertEquals(teacher.getId(), dbTeacher.getId());
        Assertions.assertEquals(teacher.getFullName(), dbTeacher.getFullName());
        Assertions.assertEquals(teacher.getAddress(), dbTeacher.getAddress());
        Assertions.assertEquals(teacher.getEmail(), dbTeacher.getEmail());
        Assertions.assertEquals(teacher.getContactNumber(), dbTeacher.getContactNumber());
        Assertions.assertEquals(teacher.getAssignedCredit(), dbTeacher.getAssignedCredit());
        Assertions.assertEquals(teacher.getDepartment().getId(), dbTeacher.getDepartment().getId());
        Assertions.assertEquals(teacher.getDesignation().getId(), dbTeacher.getDesignation().getId());

        dbTeacher.setFullName(dbTeacher.getFullName() + "U");
        dbTeacher.setAddress(dbTeacher.getAddress() + "U");
        dbTeacher.setEmail(dbTeacher.getEmail() + "U");
        dbTeacher.setContactNumber("+0000000000000");
        dbTeacher.setAssignedCredit(99);

        Designation profDesignation = designationRepository.findByTitle("Professor").orElseThrow();
        Department eeeDept = departmentRepository.findByCode("EEE").orElseThrow();
        dbTeacher.setDepartment(eeeDept);
        dbTeacher.setDesignation(profDesignation);

        repository.save(dbTeacher);

        Teacher dbTeacher2 = repository.findById(teacher.getId()).orElseThrow(IllegalStateException::new);
        Assertions.assertEquals(teacher.getId(), dbTeacher2.getId());
        Assertions.assertEquals(teacher.getFullName() + "U", dbTeacher2.getFullName());
        Assertions.assertEquals(teacher.getAddress() + "U", dbTeacher2.getAddress());
        Assertions.assertEquals(teacher.getEmail() + "U", dbTeacher2.getEmail());
        Assertions.assertEquals("+0000000000000", dbTeacher2.getContactNumber());
        Assertions.assertEquals(99, dbTeacher2.getAssignedCredit());
        Assertions.assertEquals(eeeDept.getId(), dbTeacher2.getDepartment().getId());
        Assertions.assertEquals(profDesignation.getId(), dbTeacher2.getDesignation().getId());

        repository.deleteById(dbTeacher.getId());
    }
}
