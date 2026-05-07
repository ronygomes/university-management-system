package me.ronygomes.ums.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import me.ronygomes.ums.api.model.UploadedFile;

import java.io.Serializable;

public class UploadedFileDto implements Serializable {

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    private String uploadPath;

    @Positive
    private long fileSize;

    @NotNull
    @Size(min = 1, max = 100)
    private String filetype;

    public UploadedFileDto() {
    }

    public UploadedFileDto(UploadedFile entity) {
        this.name = entity.getName();
        this.uploadPath = entity.getId().toString();
        this.fileSize = entity.getFileSize();
        this.filetype = entity.getFiletype();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }
}
