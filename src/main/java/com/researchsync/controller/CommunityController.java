package com.researchsync.controller;

import com.researchsync.model.HelpRequest;
import com.researchsync.model.User;
import com.researchsync.service.CommunityService;
import com.researchsync.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String communityHome(Model model) {
        try {
            List<HelpRequest> openRequests = communityService.getOpenHelpRequests();
            model.addAttribute("helpRequests", openRequests);
            return "community/home";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Error loading community page: " + e.getMessage());
            return "community/home";
        }
    }

    @GetMapping("/help/create")
    public String createHelpRequestForm(Model model) {
        model.addAttribute("helpRequest", new HelpRequest());
        return "community/create-help";
    }

    @PostMapping("/help/create")
    public String createHelpRequest(@ModelAttribute HelpRequest helpRequest,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            User requester = userService.findByEmail(userDetails.getUsername());

            HelpRequest createdRequest = communityService.createHelpRequest(helpRequest, requester);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Help request created successfully! Community members will be notified.");

            return "redirect:/community/help/" + createdRequest.getRequestId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Failed to create help request: " + e.getMessage());
            return "redirect:/community/help/create";
        }
    }

    @GetMapping("/help/{id}")
    public String viewHelpRequest(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            HelpRequest helpRequest = communityService.findById(id);

            model.addAttribute("helpRequest", helpRequest);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("canHelp", communityService.canUserHelp(currentUser, id));

            return "community/view-help";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Help request not found.");
            return "redirect:/community";
        }
    }

    @PostMapping("/help/{id}/offer-help")
    public String offerHelp(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            User helper = userService.findByEmail(userDetails.getUsername());

            communityService.offerHelp(id, helper);

            redirectAttributes.addFlashAttribute("successMsg",
                    "You've offered to help! The requester will be notified.");

            return "redirect:/community/help/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Failed to offer help: " + e.getMessage());
            return "redirect:/community/help/" + id;
        }
    }

    @GetMapping("/my-requests")
    public String myRequests(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            List<HelpRequest> myRequests = communityService.getUserHelpRequests(currentUser);

            model.addAttribute("helpRequests", myRequests);
            model.addAttribute("user", currentUser);

            return "community/my-requests";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Error loading your requests: " + e.getMessage());
            return "community/my-requests";
        }
    }
}
