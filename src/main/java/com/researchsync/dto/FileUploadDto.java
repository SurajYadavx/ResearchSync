// src/main/java/com/researchsync/dto/FileUploadDto.java
package com.researchsync.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class FileUploadDto {

    @NotNull(message = "Please select a file")
    private MultipartFile file;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
