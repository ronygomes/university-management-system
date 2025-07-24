package me.ronygomes.ums.api.repository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ActiveProfiles("database-test")
@Testcontainers(disabledWithoutDocker = true)
public class RegistrationNumberRepositoryTest {

    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private RegistrationNumberRepository repository;

    @Test
    @Transactional
    void testSameDepartment() throws ParseException {

        Date registrationDate = FORMATTER.parse("2026-01-31");
        String nextId = repository.getNextId(registrationDate, "CSE");
        Assertions.assertEquals("2026-CSE-0001", nextId);

        nextId = repository.getNextId(registrationDate, "CSE");
        Assertions.assertEquals("2026-CSE-0002", nextId);

        nextId = repository.getNextId(registrationDate, "CSE");
        Assertions.assertEquals("2026-CSE-0003", nextId);
    }

    @Test
    @Transactional
    void testDifferentYear() throws ParseException {

        Date rd1 = FORMATTER.parse("2025-01-31");
        String nextId = repository.getNextId(rd1, "CSE");
        Assertions.assertEquals("2025-CSE-0001", nextId);

        nextId = repository.getNextId(rd1, "CSE");
        Assertions.assertEquals("2025-CSE-0002", nextId);

        Date rd2 = FORMATTER.parse("2024-01-31");
        nextId = repository.getNextId(rd2, "CSE");
        Assertions.assertEquals("2024-CSE-0001", nextId);

        nextId = repository.getNextId(rd2, "CSE");
        Assertions.assertEquals("2024-CSE-0002", nextId);
    }

    @Test
    @Transactional
    void testDifferentDepartment() throws ParseException {

        Date registrationDate = FORMATTER.parse("2025-01-31");
        String nextId = repository.getNextId(registrationDate, "EEE");
        Assertions.assertEquals("2025-EEE-0001", nextId);

        nextId = repository.getNextId(registrationDate, "SoB");
        Assertions.assertEquals("2025-SoB-0001", nextId);

        nextId = repository.getNextId(registrationDate, "CE");
        Assertions.assertEquals("2025-CE-0001", nextId);

        nextId = repository.getNextId(registrationDate, "TE");
        Assertions.assertEquals("2025-TE-0001", nextId);
    }
}
