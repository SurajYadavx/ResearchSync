package com.researchsync.controller;

import com.researchsync.model.UploadedFile;
import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import com.researchsync.service.FileService;
import com.researchsync.service.UserService;
import com.researchsync.service.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Display file upload form
     */
    @GetMapping("/upload")
    public String uploadForm(@RequestParam(required = false) Long workspaceId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            List<Workspace> userWorkspaces = workspaceService.getUserWorkspaces(currentUser);

            model.addAttribute("workspaces", userWorkspaces);
            model.addAttribute("selectedWorkspaceId", workspaceId);

            log.info("Upload form loaded for user: {}, workspace: {}", currentUser.getEmail(), workspaceId);
            return "files/upload";

        } catch (Exception e) {
            log.error("Error loading upload form", e);
            redirectAttributes.addFlashAttribute("errorMsg", "Error loading upload form: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    /**
     * Handle file upload with comprehensive validation and logging
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam Long workspaceId,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String description,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            log.info("Upload request received for workspace: {}", workspaceId);

            // Validate file presence
            if (file == null || file.isEmpty()) {
                log.warn("No file provided in upload request");
                redirectAttributes.addFlashAttribute("errorMsg", "Please select a file to upload.");
                return "redirect:/files/upload?workspaceId=" + workspaceId;
            }

            log.info("File received: {}, Size: {} bytes, Content-Type: {}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            // Get uploader details
            User uploader = userService.findByEmail(userDetails.getUsername());
            log.info("Uploader: {} (ID: {})", uploader.getEmail(), uploader.getUserId());

            // Check workspace access
            if (!workspaceService.canUserAccessWorkspace(uploader, workspaceId)) {
                log.warn("User {} doesn't have access to workspace {}", uploader.getEmail(), workspaceId);
                redirectAttributes.addFlashAttribute("errorMsg", "You don't have access to this workspace.");
                return "redirect:/dashboard";
            }

            // Get workspace
            Workspace workspace = workspaceService.findById(workspaceId);
            log.info("Workspace found: {} (ID: {})", workspace.getName(), workspace.getWorkspaceId());

            // Upload file
            UploadedFile uploadedFile = fileService.uploadFileWithCategory(
                    file,
                    workspace,
                    uploader,
                    category != null && !category.trim().isEmpty() ? category : "Other",
                    description
            );

            log.info("File uploaded successfully with ID: {}", uploadedFile.getFileId());

            redirectAttributes.addFlashAttribute("successMsg",
                    "File '" + uploadedFile.getOriginalFilename() + "' uploaded successfully!");

            return "redirect:/workspace/view?workspaceId=" + workspaceId;

        } catch (Exception e) {
            log.error("Error uploading file to workspace {}", workspaceId, e);
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Failed to upload file: " + e.getMessage());
            return "redirect:/files/upload?workspaceId=" + workspaceId;
        }
    }

    /**
     * Download file with access control
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Download request for file ID: {}", fileId);

            User currentUser = userService.findByEmail(userDetails.getUsername());
            UploadedFile file = fileService.findById(fileId);

            // Check workspace access
            if (!workspaceService.canUserAccessWorkspace(currentUser, file.getWorkspace().getWorkspaceId())) {
                log.warn("User {} attempted to download file {} without access", currentUser.getEmail(), fileId);
                return ResponseEntity.notFound().build();
            }

            Resource resource = fileService.downloadFile(fileId);
            log.info("File {} downloaded by {}", file.getOriginalFilename(), currentUser.getEmail());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getOriginalFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading file with ID: {}", fileId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete file (optional - add if needed)
     */
    @PostMapping("/delete/{fileId}")
    public String deleteFile(@PathVariable Long fileId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            UploadedFile file = fileService.findById(fileId);
            Long workspaceId = file.getWorkspace().getWorkspaceId();

            fileService.deleteFile(fileId, currentUser);

            log.info("File {} deleted by {}", fileId, currentUser.getEmail());
            redirectAttributes.addFlashAttribute("successMsg", "File deleted successfully!");

            return "redirect:/workspace/view?workspaceId=" + workspaceId;

        } catch (Exception e) {
            log.error("Error deleting file with ID: {}", fileId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to delete file: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }
}
