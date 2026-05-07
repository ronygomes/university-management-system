package me.ronygomes.ums.api.service;

import jakarta.transaction.Transactional;
import me.ronygomes.ums.api.dto.UploadedFileDto;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.UploadedFile;
import me.ronygomes.ums.api.repository.UploadedFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static me.ronygomes.ums.api.exception.ExceptionType.DATA_VALIDATION_FAILED;

@Service
public class UploadService {

    private static final String EMPTY_FILE_DETAILS = "Uploaded file is empty";
    private static final String STORAGE_FAILURE_DETAILS = "Failed to store uploaded file";

    private final UploadedFileRepository uploadedFileRepository;
    private final String uploadDir;

    public UploadService(UploadedFileRepository uploadedFileRepository,
                         @Value("${ums.upload.dir}") String uploadDir) {

        this.uploadedFileRepository = uploadedFileRepository;
        this.uploadDir = uploadDir;
    }

    @Transactional
    public UploadedFileDto upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new UmsDataException(DATA_VALIDATION_FAILED, EMPTY_FILE_DETAILS);
        }

        UUID id = UUID.randomUUID();
        Path target = Paths.get(uploadDir, id.toString());

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new UmsDataException(DATA_VALIDATION_FAILED, STORAGE_FAILURE_DETAILS);
        }

        try {
            UploadedFile entity = new UploadedFile();
            entity.setId(id);
            entity.setName(file.getOriginalFilename());
            entity.setFileSize(file.getSize());
            entity.setFiletype(file.getContentType());

            uploadedFileRepository.save(entity);
            return new UploadedFileDto(entity);
        } catch (RuntimeException e) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
            }
            throw e;
        }
    }
}
