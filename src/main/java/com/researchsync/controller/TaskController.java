package com.researchsync.controller;

import com.researchsync.model.Task;
import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import com.researchsync.model.WorkspaceMember;
import com.researchsync.service.TaskService;
import com.researchsync.service.UserService;
import com.researchsync.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private WorkspaceService workspaceService;

    // LIST ALL TASKS FOR USER
    @GetMapping
    public String listTasks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            List<Task> assignedTasks = taskService.getTasksByUser(currentUser);
            List<Task> createdTasks = taskService.getTasksByCreator(currentUser);

            model.addAttribute("assignedTasks", assignedTasks);
            model.addAttribute("createdTasks", createdTasks);
            model.addAttribute("user", currentUser);

            return "tasks/list";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Error loading tasks: " + e.getMessage());
            return "tasks/list";
        }
    }

    // CREATE TASK FORM - ENHANCED WITH MEMBER SELECTION
    @GetMapping("/create")
    public String createTaskForm(@RequestParam(required = false) Long workspaceId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());

            if (workspaceId != null) {
                if (!workspaceService.canUserAccessWorkspace(currentUser, workspaceId)) {
                    redirectAttributes.addFlashAttribute("errorMsg", "You don't have access to this workspace.");
                    return "redirect:/dashboard";
                }

                Workspace workspace = workspaceService.findById(workspaceId);
                List<WorkspaceMember> workspaceMembers = workspaceService.getWorkspaceMembers(workspaceId);

                model.addAttribute("workspace", workspace);
                model.addAttribute("workspaceMembers", workspaceMembers);
            }

            List<Workspace> userWorkspaces = workspaceService.getUserWorkspaces(currentUser);
            model.addAttribute("workspaces", userWorkspaces);
            model.addAttribute("task", new Task());

            return "tasks/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error loading task form: " + e.getMessage());
            return "redirect:/tasks";
        }
    }

    // CREATE TASK - ENHANCED WITH EMAIL ASSIGNMENT
    @PostMapping("/create")
    public String createTask(@ModelAttribute Task task,
                             @RequestParam Long workspaceId,
                             @RequestParam(required = false) String assignToEmail,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User creator = userService.findByEmail(userDetails.getUsername());
            Workspace workspace = workspaceService.findById(workspaceId);

            if (!workspaceService.canUserAccessWorkspace(creator, workspaceId)) {
                redirectAttributes.addFlashAttribute("errorMsg", "You don't have access to this workspace.");
                return "redirect:/dashboard";
            }

            task.setWorkspace(workspace);

            // Assign to user by email if provided
            if (assignToEmail != null && !assignToEmail.trim().isEmpty()) {
                try {
                    User assignedUser = userService.findByEmail(assignToEmail.trim());

                    // Check if user is member of workspace
                    if (workspaceService.canUserAccessWorkspace(assignedUser, workspaceId)) {
                        task.setAssignedTo(assignedUser);
                    } else {
                        redirectAttributes.addFlashAttribute("errorMsg",
                                "User " + assignToEmail + " is not a member of this workspace.");
                        return "redirect:/tasks/create?workspaceId=" + workspaceId;
                    }
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMsg",
                            "User with email " + assignToEmail + " not found.");
                    return "redirect:/tasks/create?workspaceId=" + workspaceId;
                }
            }

            Task createdTask = taskService.createTask(task, creator);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Task '" + task.getTitle() + "' created successfully!");

            return "redirect:/workspace/" + workspaceId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Failed to create task: " + e.getMessage());
            return "redirect:/tasks/create?workspaceId=" + workspaceId;
        }
    }

    // VIEW TASK
    @GetMapping("/{id}")
    public String viewTask(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            Task task = taskService.findById(id);

            if (!workspaceService.canUserAccessWorkspace(currentUser, task.getWorkspace().getWorkspaceId())) {
                redirectAttributes.addFlashAttribute("errorMsg", "You don't have access to this task.");
                return "redirect:/dashboard";
            }

            model.addAttribute("task", task);
            model.addAttribute("currentUser", currentUser);

            return "tasks/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Task not found.");
            return "redirect:/tasks";
        }
    }

    // UPDATE TASK STATUS
    @PostMapping("/{id}/status")
    public String updateTaskStatus(@PathVariable Long id,
                                   @RequestParam Task.TaskStatus status,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            Task updatedTask = taskService.updateTaskStatus(id, status, currentUser);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Task status updated to " + status);

            return "redirect:/tasks/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Failed to update task status: " + e.getMessage());
            return "redirect:/tasks/" + id;
        }
    }

    // ASSIGN TASK TO USER BY EMAIL - NEW ENDPOINT
    @PostMapping("/{id}/assign")
    public String assignTask(@PathVariable Long id,
                             @RequestParam String email,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            User assignee = userService.findByEmail(email);

            Task updatedTask = taskService.assignTask(id, assignee.getUserId(), currentUser);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Task assigned to " + assignee.getName() + " successfully!");

            return "redirect:/tasks/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Failed to assign task: " + e.getMessage());
            return "redirect:/tasks/" + id;
        }
    }

    // GET WORKSPACE MEMBERS API (for dynamic loading)
    @GetMapping("/api/workspace/{workspaceId}/members")
    @ResponseBody
    public List<WorkspaceMember> getWorkspaceMembers(@PathVariable Long workspaceId,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());

            if (!workspaceService.canUserAccessWorkspace(currentUser, workspaceId)) {
                return List.of();
            }

            return workspaceService.getWorkspaceMembers(workspaceId);
        } catch (Exception e) {
            return List.of();
        }
    }
}
