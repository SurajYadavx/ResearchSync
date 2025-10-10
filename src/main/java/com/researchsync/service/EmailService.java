package com.researchsync.service;

import com.researchsync.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@researchsync.com}")
    private String fromEmail;

    public void sendVerificationEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("ResearchSync - Email Verification");
            message.setText("Hello " + user.getName() + ",\n\n" +
                    "Thank you for registering with ResearchSync!\n\n" +
                    "Please click the following link to verify your email address:\n" +
                    "http://localhost:8081/verify-email?token=" + user.getVerificationToken() + "\n\n" +
                    "If you didn't create this account, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "ResearchSync Team");

            mailSender.send(message);
            System.out.println("✅ Verification email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
        }
    }

    public void sendWorkspaceInvitation(User user, String workspaceName, String inviterName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("ResearchSync - Workspace Invitation");
            message.setText("Hello " + user.getName() + ",\n\n" +
                    inviterName + " has invited you to join the workspace '" + workspaceName + "' on ResearchSync.\n\n" +
                    "Please log in to your account to accept or decline this invitation:\n" +
                    "http://localhost:8081/dashboard\n\n" +
                    "Best regards,\n" +
                    "ResearchSync Team");

            mailSender.send(message);
            System.out.println("✅ Workspace invitation sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send workspace invitation email: " + e.getMessage());
        }
    }

    public void sendTaskAssignmentNotification(User assignee, String taskTitle, String workspaceName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(assignee.getEmail());
            message.setSubject("ResearchSync - New Task Assigned");
            message.setText("Hello " + assignee.getName() + ",\n\n" +
                    "A new task has been assigned to you!\n\n" +
                    "Task: " + taskTitle + "\n" +
                    "Workspace: " + workspaceName + "\n\n" +
                    "Please log in to view task details and start working:\n" +
                    "http://localhost:8081/dashboard\n\n" +
                    "Best regards,\n" +
                    "ResearchSync Team");

            mailSender.send(message);
            System.out.println("✅ Task assignment notification sent to: " + assignee.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send task assignment notification: " + e.getMessage());
        }
    }

    public void sendDeadlineAlert(User user, String taskTitle, String workspaceName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("ResearchSync - Task Deadline Alert");
            message.setText("Hello " + user.getName() + ",\n\n" +
                    "⚠️ REMINDER: Your task deadline is approaching!\n\n" +
                    "Task: " + taskTitle + "\n" +
                    "Workspace: " + workspaceName + "\n\n" +
                    "Please complete this task soon to avoid missing the deadline.\n\n" +
                    "Log in to update task status:\n" +
                    "http://localhost:8081/dashboard\n\n" +
                    "Best regards,\n" +
                    "ResearchSync Team");

            mailSender.send(message);
            System.out.println("✅ Deadline alert sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send deadline alert: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("ResearchSync - Password Reset");
            message.setText("Hello " + user.getName() + ",\n\n" +
                    "You requested to reset your password. Please click the following link:\n" +
                    "http://localhost:8081/reset-password?token=" + resetToken + "\n\n" +
                    "If you didn't request this, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "ResearchSync Team");

            mailSender.send(message);
            System.out.println("✅ Password reset email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset email: " + e.getMessage());
        }
    }

    public void sendMeetingNotification(User user, String meetingTitle, LocalDateTime scheduledTime, String meetingLink, String workspaceName) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
            String formattedTime = scheduledTime.format(formatter);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("ResearchSync - Meeting Scheduled: " + meetingTitle);
            message.setText("Hello " + user.getName() + ",\n\n" +
                    "A new meeting has been scheduled in workspace '" + workspaceName + "':\n\n" +
                    "Meeting: " + meetingTitle + "\n" +
                    "Time: " + formattedTime + "\n" +
                    "Meeting Link: " + (meetingLink != null ? meetingLink : "To be provided") + "\n\n" +
                    "Please mark your calendar and join on time.\n\n" +
                    "Best regards,\n" +
                    "ResearchSync Team");

            mailSender.send(message);
            System.out.println("✅ Meeting notification sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send meeting notification: " + e.getMessage());
        }
    }

    public void sendHelpOfferNotification(User requester, User helper, String requestTitle) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(requester.getEmail());
            message.setSubject("ResearchSync - Someone Wants to Help!");
            message.setText("Hello " + requester.getName() + ",\n\n" +
                    "Good news! " + helper.getName() + " has offered to help with your request:\n\n" +
                    "Request: " + requestTitle + "\n\n" +
                    "Please log in to your account to accept the help and provide access details:\n" +
                    "http://localhost:8081/community\n\n" +
                    "Best regards,\n" +
                    "ResearchSync Team");

            mailSender.send(message);
            System.out.println("✅ Help offer notification sent to: " + requester.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send help offer notification: " + e.getMessage());
        }
    }

    public void sendHelpThankYouNotification(User helper, User requester, String requestTitle) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(helper.getEmail());
            message.setSubject("ResearchSync - Thank You for Your Help!");
            message.setText("Hello " + helper.getName() + ",\n\n" +
                    "Thank you for helping " + requester.getName() + " with their request:\n\n" +
                    "Request: " + requestTitle + "\n\n" +
                    "Your contribution to the community is greatly appreciated!\n" +
                    "Your help has been marked as resolved and your reputation score has been updated.\n\n" +
                    "Keep up the great work!\n\n" +
                    "Best regards,\n" +
                    "ResearchSync Team");

            mailSender.send(message);
            System.out.println("✅ Thank you notification sent to: " + helper.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send thank you notification: " + e.getMessage());
        }
    }
}
