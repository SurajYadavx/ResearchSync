package com.researchsync.controller;

import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import com.researchsync.model.WorkspaceMember;
import com.researchsync.service.UserService;
import com.researchsync.service.WorkspaceService;
import com.researchsync.exception.WorkspaceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/workspace")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private UserService userService;

    // CREATE WORKSPACE - GET
    @GetMapping("/create")
    public String createWorkspaceForm(Model model) {
        model.addAttribute("workspace", new Workspace());
        return "workspace/create";
    }

    // CREATE WORKSPACE - POST
    @PostMapping("/create")
    public String createWorkspace(@ModelAttribute Workspace workspace,
                                  BindingResult result,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                return "workspace/create";
            }

            User creator = userService.findByEmail(userDetails.getUsername());
            Workspace createdWorkspace = workspaceService.createWorkspace(workspace, creator);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Workspace '" + workspace.getName() + "' created successfully!");

            // REDIRECT TO WORKSPACE VIEW (this was missing!)
            return "redirect:/workspace/" + createdWorkspace.getWorkspaceId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Failed to create workspace: " + e.getMessage());
            return "redirect:/workspace/create";
        }
    }

    // VIEW WORKSPACE
    @GetMapping("/{id}")
    public String viewWorkspace(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());

            // Check if user has access to this workspace
            if (!workspaceService.canUserAccessWorkspace(currentUser, id)) {
                redirectAttributes.addFlashAttribute("errorMsg",
                        "You don't have access to this workspace.");
                return "redirect:/dashboard";
            }

            Workspace workspace = workspaceService.findById(id);
            List<WorkspaceMember> members = workspaceService.getWorkspaceMembers(id);
            boolean isAdmin = workspaceService.isUserAdminOfWorkspace(currentUser, id);

            model.addAttribute("workspace", workspace);
            model.addAttribute("members", members);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("currentUser", currentUser);

            return "workspace/view";

        } catch (WorkspaceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Workspace not found.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Error loading workspace: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    // INVITE MEMBERS
    @PostMapping("/{id}/invite")
    public String inviteMembers(@PathVariable Long id,
                                @RequestParam String emails,
                                @RequestParam(defaultValue = "MEMBER") WorkspaceMember.Role role,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User inviter = userService.findByEmail(userDetails.getUsername());

            if (!workspaceService.isUserAdminOfWorkspace(inviter, id)) {
                redirectAttributes.addFlashAttribute("errorMsg",
                        "Only workspace admins can invite members.");
                return "redirect:/workspace/" + id;
            }

            String[] emailArray = emails.split(",");
            int successCount = 0;

            for (String email : emailArray) {
                try {
                    workspaceService.inviteUserToWorkspace(id, email.trim(), role, inviter);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("Failed to invite " + email + ": " + e.getMessage());
                }
            }

            redirectAttributes.addFlashAttribute("successMsg",
                    successCount + " invitation(s) sent successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Failed to send invitations: " + e.getMessage());
        }

        return "redirect:/workspace/" + id;
    }
}
