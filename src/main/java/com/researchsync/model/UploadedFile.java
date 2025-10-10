package com.researchsync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @NotBlank(message = "Filename is required")
    @Size(max = 255, message = "Filename cannot exceed 255 characters")
    @Column(nullable = false)
    private String filename;

    @NotBlank(message = "Original filename is required")
    @Size(max = 255, message = "Original filename cannot exceed 255 characters")
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "uploaded_date", nullable = false)
    private LocalDateTime uploadedDate;

    @Column(name = "is_active")
    private boolean isActive = true;

    // Constructors
    public UploadedFile() {
        this.uploadedDate = LocalDateTime.now();
    }

    public UploadedFile(String filename, String originalFilename, Workspace workspace, User uploadedBy) {
        this();
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.workspace = workspace;
        this.uploadedBy = uploadedBy;
    }

    // Getters and Setters
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getUploadedDate() { return uploadedDate; }
    public void setUploadedDate(LocalDateTime uploadedDate) { this.uploadedDate = uploadedDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Utility methods
    public String getFileExtension() {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";

        if (fileSize < 1024) return fileSize + " B";
        else if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        else if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        else return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }

    public boolean isImageFile() {
        String extension = getFileExtension();
        return extension.matches("jpg|jpeg|png|gif|bmp|svg");
    }

    public boolean isDocumentFile() {
        String extension = getFileExtension();
        return extension.matches("pdf|doc|docx|txt|rtf|odt");
    }
    @Column(name = "category")
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "UploadedFile{" +
                "fileId=" + fileId +
                ", originalFilename='" + originalFilename + '\'' +
                ", workspace=" + (workspace != null ? workspace.getName() : "null") +
                ", uploadedBy=" + (uploadedBy != null ? uploadedBy.getName() : "null") +
                ", fileSize=" + getFormattedFileSize() +
                ", uploadedDate=" + uploadedDate +
                '}';
    }
}
