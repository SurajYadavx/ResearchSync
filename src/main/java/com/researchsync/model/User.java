package com.researchsync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType = UserType.STUDENT;

    @Size(max = 200, message = "University name cannot exceed 200 characters")
    private String university;

    @Size(max = 200, message = "Department name cannot exceed 200 characters")
    private String department;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    // Relationships
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Workspace> createdWorkspaces = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkspaceMember> workspaceMemberships = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> assignedTasks = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> createdTasks = new ArrayList<>();

    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UploadedFile> uploadedFiles = new ArrayList<>();

    // Constructors
    public User() {
        this.createdDate = LocalDateTime.now();
    }

    public User(String name, String email, String password, UserType userType) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
        this.userType = userType;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public List<Workspace> getCreatedWorkspaces() { return createdWorkspaces; }
    public void setCreatedWorkspaces(List<Workspace> createdWorkspaces) { this.createdWorkspaces = createdWorkspaces; }

    public List<WorkspaceMember> getWorkspaceMemberships() { return workspaceMemberships; }
    public void setWorkspaceMemberships(List<WorkspaceMember> workspaceMemberships) { this.workspaceMemberships = workspaceMemberships; }

    public List<Task> getAssignedTasks() { return assignedTasks; }
    public void setAssignedTasks(List<Task> assignedTasks) { this.assignedTasks = assignedTasks; }

    public List<Task> getCreatedTasks() { return createdTasks; }
    public void setCreatedTasks(List<Task> createdTasks) { this.createdTasks = createdTasks; }

    public List<UploadedFile> getUploadedFiles() { return uploadedFiles; }
    public void setUploadedFiles(List<UploadedFile> uploadedFiles) { this.uploadedFiles = uploadedFiles; }

    // Utility methods
    public void addCreatedWorkspace(Workspace workspace) {
        createdWorkspaces.add(workspace);
        workspace.setCreator(this);
    }

    public void addWorkspaceMembership(WorkspaceMember membership) {
        workspaceMemberships.add(membership);
        membership.setUser(this);
    }

    public void addAssignedTask(Task task) {
        assignedTasks.add(task);
        task.setAssignedTo(this);
    }

    public void addCreatedTask(Task task) {
        createdTasks.add(task);
        task.setCreatedBy(this);
    }

    public void addUploadedFile(UploadedFile file) {
        uploadedFiles.add(file);
        file.setUploadedBy(this);
    }

    public boolean isProfessor() {
        return userType == UserType.PROFESSOR;
    }

    public boolean isStudent() {
        return userType == UserType.STUDENT;
    }

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    // Enum
    public enum UserType {
        STUDENT, PROFESSOR, ADMIN
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", userType=" + userType +
                ", university='" + university + '\'' +
                ", department='" + department + '\'' +
                ", isVerified=" + isVerified +
                ", createdDate=" + createdDate +
                '}';
    }
}
