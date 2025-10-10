//package com.researchsync.repository;
//
//
//
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//
//import com.researchsync.model.UploadedFile;
//import com.researchsync.model.User;
//import com.researchsync.model.Workspace;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
//
//    List<UploadedFile> findByWorkspaceAndIsActiveTrue(Workspace workspace);
//    List<UploadedFile> findByWorkspaceAndIsActiveTrueOrderByUploadedDateDesc(Workspace workspace);
//    List<UploadedFile> findByUploadedByAndIsActiveTrue(User uploadedBy);
//    List<UploadedFile> findByWorkspaceAndUploadedBy(Workspace workspace, User uploadedBy);
//    List<UploadedFile> findByOriginalFilenameContainingIgnoreCaseAndIsActiveTrue(String filename);
//    List<UploadedFile> findByUploadedBy(User user);
//    // NEW: Category-based queries
//    List<UploadedFile> findByWorkspaceAndCategoryAndIsActiveTrue(Workspace workspace, String category);
//    List<UploadedFile> findByCategoryAndIsActiveTrueOrderByUploadedDateDesc(String category);
//
//    @Query("SELECT f FROM UploadedFile f WHERE f.workspace = :workspace AND f.uploadedDate >= :startDate AND f.isActive = true ORDER BY f.uploadedDate DESC")
//    List<UploadedFile> findRecentFilesByWorkspace(@Param("workspace") Workspace workspace, @Param("startDate") LocalDateTime startDate);
//
//    @Query("SELECT COUNT(f) FROM UploadedFile f WHERE f.workspace = :workspace AND f.isActive = true")
//    Long countActiveFilesByWorkspace(@Param("workspace") Workspace workspace);
//
//    @Query("SELECT SUM(f.fileSize) FROM UploadedFile f WHERE f.workspace = :workspace AND f.isActive = true")
//    Long getTotalFileSizeByWorkspace(@Param("workspace") Workspace workspace);
//
//    List<UploadedFile> findByContentTypeContainingAndIsActiveTrue(String contentType);
//}


// src/main/java/com/researchsync/repository/UploadedFileRepository.java
package com.researchsync.repository;

import com.researchsync.model.UploadedFile;
import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findByUploadedByAndIsActiveTrue(User uploadedBy);
    List<UploadedFile> findByWorkspaceAndIsActiveTrueOrderByUploadedDateDesc(Workspace workspace);
    List<UploadedFile> findByWorkspaceAndCategoryAndIsActiveTrue(Workspace workspace, String category);
    List<UploadedFile> findByOriginalFilenameContainingIgnoreCaseAndIsActiveTrue(String filename);
    List<UploadedFile> findByWorkspaceAndIsActiveTrue(Workspace workspace);

    @Query("SELECT f FROM UploadedFile f WHERE f.workspace = :workspace AND f.uploadedDate >= :startDate AND f.isActive = true ORDER BY f.uploadedDate DESC")
    List<UploadedFile> findRecentFilesByWorkspace(@Param("workspace") Workspace workspace, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(f) FROM UploadedFile f WHERE f.workspace = :workspace AND f.isActive = true")
    Long countActiveFilesByWorkspace(@Param("workspace") Workspace workspace);

    @Query("SELECT SUM(f.fileSize) FROM UploadedFile f WHERE f.workspace = :workspace AND f.isActive = true")
    Long getTotalFileSizeByWorkspace(@Param("workspace") Workspace workspace);

    List<UploadedFile> findByCategoryAndIsActiveTrueOrderByUploadedDateDesc(String category);
    List<UploadedFile> findByContentTypeContainingAndIsActiveTrue(String contentType);
}
