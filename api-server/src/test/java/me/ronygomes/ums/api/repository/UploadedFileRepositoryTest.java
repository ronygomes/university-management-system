package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.model.UploadedFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("database-test")
@Testcontainers(disabledWithoutDocker = true)
public class UploadedFileRepositoryTest {

    @Autowired
    private UploadedFileRepository repository;

    @Test
    void testUploadedFileRepositoryIsNotNull() {
        Assertions.assertNotNull(repository);
    }

    @Test
    void testCanSaveAndFindById() {
        UploadedFile file = new UploadedFile();
        file.setId(UUID.randomUUID());
        file.setName("transcript.pdf");
        file.setFileSize(12345L);
        file.setFiletype("application/pdf");

        repository.save(file);

        Optional<UploadedFile> dbFileOpt = repository.findById(file.getId());
        Assertions.assertTrue(dbFileOpt.isPresent());

        UploadedFile dbFile = dbFileOpt.get();
        Assertions.assertEquals(file.getId(), dbFile.getId());
        Assertions.assertEquals("transcript.pdf", dbFile.getName());
        Assertions.assertEquals(12345L, dbFile.getFileSize());
        Assertions.assertEquals("application/pdf", dbFile.getFiletype());
    }
}
