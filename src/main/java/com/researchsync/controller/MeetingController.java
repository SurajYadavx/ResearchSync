package com.researchsync.controller;

import com.researchsync.model.Meeting;
import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import com.researchsync.service.MeetingService;
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

@Controller
@RequestMapping("/meetings")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private UserService userService;

    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/create")
    public String createMeetingForm(@RequestParam Long workspaceId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());

            if (!workspaceService.canUserAccessWorkspace(currentUser, workspaceId)) {
                redirectAttributes.addFlashAttribute("errorMsg", "You don't have access to this workspace.");
                return "redirect:/dashboard";
            }

            Workspace workspace = workspaceService.findById(workspaceId);
            model.addAttribute("workspace", workspace);
            model.addAttribute("meeting", new Meeting());

            return "meetings/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error loading meeting form: " + e.getMessage());
            return "redirect:/workspace/" + workspaceId;
        }
    }

    @PostMapping("/create")
    public String createMeeting(@ModelAttribute Meeting meeting,
                                @RequestParam Long workspaceId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User creator = userService.findByEmail(userDetails.getUsername());
            Workspace workspace = workspaceService.findById(workspaceId);

            Meeting createdMeeting = meetingService.createMeeting(meeting, workspace, creator);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Meeting '" + meeting.getTitle() + "' scheduled successfully!");

            return "redirect:/workspace/" + workspaceId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Failed to create meeting: " + e.getMessage());
            return "redirect:/meetings/create?workspaceId=" + workspaceId;
        }
    }

    @GetMapping("/workspace/{workspaceId}")
    public String listMeetings(@PathVariable Long workspaceId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());

            if (!workspaceService.canUserAccessWorkspace(currentUser, workspaceId)) {
                redirectAttributes.addFlashAttribute("errorMsg", "You don't have access to this workspace.");
                return "redirect:/dashboard";
            }

            Workspace workspace = workspaceService.findById(workspaceId);
            List<Meeting> meetings = meetingService.getWorkspaceMeetings(workspaceId);

            model.addAttribute("workspace", workspace);
            model.addAttribute("meetings", meetings);

            return "meetings/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error loading meetings: " + e.getMessage());
            return "redirect:/workspace/" + workspaceId;
        }
    }
}
