package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Designation;
import me.ronygomes.ums.api.model.Teacher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import static me.ronygomes.ums.api.helper.DataHelper.validPersistableDepartment;

@SpringBootTest
@ActiveProfiles("integration-test")
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
        Department departmentNew = validPersistableDepartment();
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
}
