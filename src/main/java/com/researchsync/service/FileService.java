package com.researchsync.service;

import com.researchsync.model.UploadedFile;
import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import com.researchsync.repository.UploadedFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private WorkspaceService workspaceService;

    @Value("${app.upload.dir:./uploads/}")
    private String uploadDir;

    /**
     * Upload file with category and description
     */
    @Transactional
    public UploadedFile uploadFileWithCategory(MultipartFile file, Workspace workspace,
                                               User uploader, String category, String description) {
        try {
            log.info("Starting file upload: {} for workspace: {}",
                    file.getOriginalFilename(), workspace.getName());

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save file to disk
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved to disk: {}", filePath.toAbsolutePath());

            // Create and populate file record
            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.setFilename(uniqueFilename);
            uploadedFile.setOriginalFilename(originalFilename);
            uploadedFile.setDescription(description);
            uploadedFile.setCategory(category != null && !category.trim().isEmpty() ? category : "Other");
            uploadedFile.setContentType(file.getContentType());
            uploadedFile.setFileSize(file.getSize());
            uploadedFile.setFilePath(filePath.toString());
            uploadedFile.setWorkspace(workspace);
            uploadedFile.setUploadedBy(uploader);
            uploadedFile.setUploadedDate(LocalDateTime.now());
            uploadedFile.setActive(true);

            // Save to database
            UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);
            log.info("File record saved to database with ID: {}", savedFile.getFileId());

            return savedFile;

        } catch (IOException e) {
            log.error("IO error during file upload", e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    public UploadedFile uploadFile(MultipartFile file, Workspace workspace,
                                   User uploader, String description) {
        return uploadFileWithCategory(file, workspace, uploader, "Other", description);
    }

    /**
     * Get all active files uploaded by a specific user
     */
    public List<UploadedFile> getUserFiles(User user) {
        log.debug("Fetching files for user: {}", user.getEmail());
        return uploadedFileRepository.findByUploadedByAndIsActiveTrue(user);
    }

    /**
     * Find file by ID
     */
    public UploadedFile findById(Long fileId) {
        log.debug("Finding file with ID: {}", fileId);
        return uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> {
                    log.error("File not found with ID: {}", fileId);
                    return new RuntimeException("File not found with ID: " + fileId);
                });
    }

    /**
     * Get workspace files by category
     */
    public List<UploadedFile> getWorkspaceFilesByCategory(Long workspaceId, String category) {
        log.debug("Fetching files for workspace: {} with category: {}", workspaceId, category);
        Workspace workspace = workspaceService.findById(workspaceId);
        return uploadedFileRepository.findByWorkspaceAndCategoryAndIsActiveTrue(workspace, category);
    }

    /**
     * Get all active workspace files
     */
    public List<UploadedFile> getWorkspaceFiles(Long workspaceId) {
        log.debug("Fetching all files for workspace: {}", workspaceId);
        Workspace workspace = workspaceService.findById(workspaceId);
        return uploadedFileRepository.findByWorkspaceAndIsActiveTrueOrderByUploadedDateDesc(workspace);
    }

    /**
     * Download file as Resource
     */
    public Resource downloadFile(Long fileId) {
        try {
            UploadedFile file = findById(fileId);
            Path filePath = Paths.get(file.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("File {} downloaded successfully", file.getOriginalFilename());
                return resource;
            } else {
                log.error("File not found or not readable: {}", filePath);
                throw new RuntimeException("File not found or not readable");
            }
        } catch (Exception e) {
            log.error("Error downloading file with ID: {}", fileId, e);
            throw new RuntimeException("Failed to download file: " + e.getMessage());
        }
    }

    /**
     * Delete file (soft delete - marks as inactive)
     */
    @Transactional
    public void deleteFile(Long fileId, User deleter) {
        try {
            UploadedFile file = findById(fileId);

            // Check permissions
            if (!file.getUploadedBy().getUserId().equals(deleter.getUserId()) &&
                    !workspaceService.isUserAdminOfWorkspace(deleter, file.getWorkspace().getWorkspaceId())) {
                log.warn("User {} attempted to delete file {} without permission",
                        deleter.getEmail(), fileId);
                throw new RuntimeException("You don't have permission to delete this file");
            }

            // Soft delete - mark as inactive
            file.setActive(false);
            uploadedFileRepository.save(file);
            log.info("File {} marked as inactive by {}", fileId, deleter.getEmail());

            // Optionally delete from disk
            try {
                Path filePath = Paths.get(file.getFilePath());
                if (Files.deleteIfExists(filePath)) {
                    log.info("File deleted from disk: {}", filePath);
                }
            } catch (IOException e) {
                log.warn("Warning: Failed to delete file from disk: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error deleting file with ID: {}", fileId, e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * Get all active files by category
     */
    public List<UploadedFile> getFilesByCategory(String category) {
        log.debug("Fetching files with category: {}", category);
        return uploadedFileRepository.findByCategoryAndIsActiveTrueOrderByUploadedDateDesc(category);
    }

    /**
     * Search files by filename
     */
    public List<UploadedFile> searchFiles(String searchTerm) {
        log.debug("Searching files with term: {}", searchTerm);
        return uploadedFileRepository.findByOriginalFilenameContainingIgnoreCaseAndIsActiveTrue(searchTerm);
    }

    /**
     * Get file count for a workspace
     */
    public long getWorkspaceFileCount(Long workspaceId) {
        Workspace workspace = workspaceService.findById(workspaceId);
        return uploadedFileRepository.findByWorkspaceAndIsActiveTrue(workspace).size();
    }

    /**
     * Get total storage used by a workspace (in bytes)
     */
    public long getWorkspaceStorageUsed(Long workspaceId) {
        Workspace workspace = workspaceService.findById(workspaceId);
        return uploadedFileRepository.findByWorkspaceAndIsActiveTrue(workspace)
                .stream()
                .mapToLong(UploadedFile::getFileSize)
                .sum();
    }
}
