package com.researchsync.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WorkspaceNotFoundException.class)
    public String handleWorkspaceNotFound(WorkspaceNotFoundException e,
                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, Model model) {
        model.addAttribute("errorMsg", "An unexpected error occurred: " + e.getMessage());
        return "error";
    }
}



