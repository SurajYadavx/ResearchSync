package com.researchsync.controller;

import com.researchsync.model.User;
import com.researchsync.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("msg", "You have been logged out successfully");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute User user, Model model) {
        try {
            System.out.println("🎯 Registration attempt for: " + user.getEmail());

            userService.registerUser(user);

            System.out.println("✅ User registered successfully: " + user.getEmail());
            model.addAttribute("successMsg", "Registration successful! You can now log in.");
            return "auth/login";

        } catch (Exception e) {
            System.err.println("❌ Registration failed: " + e.getMessage());
            model.addAttribute("errorMsg", "Registration failed: " + e.getMessage());
            model.addAttribute("user", user);
            return "auth/register";
        }
    }
}
