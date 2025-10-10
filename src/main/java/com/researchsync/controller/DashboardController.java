package com.researchsync.controller;

import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import com.researchsync.service.UserService;
import com.researchsync.service.WorkspaceService;
import com.researchsync.service.TaskService;
import com.researchsync.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FileService fileService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            List<Workspace> userWorkspaces = workspaceService.getUserWorkspaces(currentUser);

            // Calculate statistics
            Long workspaceCount = workspaceService.countUserWorkspaces(currentUser);
            Long taskCount = taskService.getTasksByUser(currentUser).size() + 0L;
            Long completedTaskCount = taskService.getCompletedTaskCountByUser(currentUser);
            Long fileCount = fileService.getUserFiles(currentUser).size() + 0L;

            model.addAttribute("user", currentUser);
            model.addAttribute("workspaces", userWorkspaces);
            model.addAttribute("workspaceCount", workspaceCount);
            model.addAttribute("taskCount", taskCount);
            model.addAttribute("completedTaskCount", completedTaskCount);
            model.addAttribute("fileCount", fileCount);
            model.addAttribute("memberCount", 0L); // Placeholder for team members count

            return "dashboard/index";

        } catch (Exception e) {
            model.addAttribute("errorMsg", "Error loading dashboard: " + e.getMessage());
            return "dashboard/index";
        }
    }

    // PROFILE PAGE
    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());

            // Calculate user statistics
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("workspaceCount", workspaceService.countUserWorkspaces(currentUser));
            userStats.put("assignedTaskCount", taskService.getTasksByUser(currentUser).size());
            userStats.put("completedTaskCount", taskService.getCompletedTaskCountByUser(currentUser));
            userStats.put("fileCount", fileService.getUserFiles(currentUser).size());

            model.addAttribute("user", currentUser);
            model.addAttribute("userStats", userStats);

            return "dashboard/profile";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Error loading profile: " + e.getMessage());
            return "dashboard/profile";
        }
    }

    // UPDATE PROFILE
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User user,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());

            // Update only allowed fields
            currentUser.setName(user.getName());
            currentUser.setUniversity(user.getUniversity());
            currentUser.setDepartment(user.getDepartment());

            userService.saveUser(currentUser);

            redirectAttributes.addFlashAttribute("successMsg", "Profile updated successfully!");
            return "redirect:/dashboard/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update profile: " + e.getMessage());
            return "redirect:/dashboard/profile";
        }
    }

    // CHANGE PASSWORD
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMsg", "New passwords do not match!");
                return "redirect:/dashboard/profile";
            }

            User currentUser = userService.findByEmail(userDetails.getUsername());

            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMsg", "Current password is incorrect!");
                return "redirect:/dashboard/profile";
            }

            currentUser.setPassword(passwordEncoder.encode(newPassword));
            userService.saveUser(currentUser);

            redirectAttributes.addFlashAttribute("successMsg", "Password changed successfully!");
            return "redirect:/dashboard/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to change password: " + e.getMessage());
            return "redirect:/dashboard/profile";
        }
    }

    // SETTINGS PAGE (Optional)
    @GetMapping("/settings")
    public String settingsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("user", currentUser);
            return "dashboard/settings";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Error loading settings: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }
}
