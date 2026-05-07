package me.ronygomes.ums.api.service;

import me.ronygomes.ums.api.dto.UploadedFileDto;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.UploadedFile;
import me.ronygomes.ums.api.repository.UploadedFileRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class UploadServiceTest {

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @TempDir
    Path tempDir;

    private UploadService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new UploadService(uploadedFileRepository, tempDir.toString());
    }

    @Test
    void testUploadSuccess() throws IOException {
        byte[] content = "hello world".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transcript.pdf",
                "application/pdf",
                content);

        UploadedFileDto dto = service.upload(file);

        Assertions.assertEquals("transcript.pdf", dto.getName());
        Assertions.assertEquals(content.length, dto.getFileSize());
        Assertions.assertEquals("application/pdf", dto.getFiletype());
        Assertions.assertNotNull(dto.getUploadPath());

        UUID id = UUID.fromString(dto.getUploadPath());
        Path stored = tempDir.resolve(id.toString());
        Assertions.assertTrue(Files.exists(stored));
        Assertions.assertArrayEquals(content, Files.readAllBytes(stored));

        ArgumentCaptor<UploadedFile> ac = ArgumentCaptor.forClass(UploadedFile.class);
        Mockito.verify(uploadedFileRepository, Mockito.times(1)).save(ac.capture());

        UploadedFile saved = ac.getValue();
        Assertions.assertEquals(id, saved.getId());
        Assertions.assertEquals("transcript.pdf", saved.getName());
        Assertions.assertEquals(content.length, saved.getFileSize());
        Assertions.assertEquals("application/pdf", saved.getFiletype());
    }

    @Test
    void testUploadEmptyFileFailure() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]);

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class,
                () -> service.upload(file));

        Assertions.assertEquals("Uploaded file is empty", ex.getErrorDetails());
        Mockito.verify(uploadedFileRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void testUploadDbFailureCleansUpFile() throws IOException {
        byte[] content = "data".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "x.bin",
                "application/octet-stream",
                content);

        Mockito.when(uploadedFileRepository.save(Mockito.any()))
                .thenThrow(new RuntimeException("db down"));

        Assertions.assertThrows(RuntimeException.class, () -> service.upload(file));

        try (var stream = Files.list(tempDir)) {
            Assertions.assertEquals(0L, stream.count());
        }
    }
}
