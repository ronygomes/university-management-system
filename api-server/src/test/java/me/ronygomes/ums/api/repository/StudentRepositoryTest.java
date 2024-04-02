package me.ronygomes.ums.api.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration-test")
public class StudentRepositoryTest {

    @Autowired
    private StudentRepository repository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @AfterEach
    void tearDown() {
        Assertions.assertEquals(0, repository.findAll().size());
    }
}
