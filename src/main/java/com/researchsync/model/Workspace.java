package com.researchsync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workspaces")
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workspaceId;

    @NotBlank(message = "Workspace name is required")
    @Size(min = 2, max = 100, message = "Workspace name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_level")
    private PrivacyLevel privacyLevel = PrivacyLevel.PRIVATE;

    // Relationships
    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkspaceMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UploadedFile> uploadedFiles = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Meeting> meetings = new ArrayList<>();

    // Constructors
    public Workspace() {
        this.createdDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    public Workspace(String name, String description, User creator) {
        this();
        this.name = name;
        this.description = description;
        this.creator = creator;
    }

    // Getters and Setters
    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public PrivacyLevel getPrivacyLevel() { return privacyLevel; }
    public void setPrivacyLevel(PrivacyLevel privacyLevel) { this.privacyLevel = privacyLevel; }

    public List<WorkspaceMember> getMembers() { return members; }
    public void setMembers(List<WorkspaceMember> members) { this.members = members; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    public List<UploadedFile> getUploadedFiles() { return uploadedFiles; }
    public void setUploadedFiles(List<UploadedFile> uploadedFiles) { this.uploadedFiles = uploadedFiles; }

    public List<Meeting> getMeetings() { return meetings; }
    public void setMeetings(List<Meeting> meetings) { this.meetings = meetings; }

    // Utility Methods
    public void addMember(WorkspaceMember member) {
        members.add(member);
        member.setWorkspace(this);
    }

    public void addTask(Task task) {
        tasks.add(task);
        task.setWorkspace(this);
    }

    public void addFile(UploadedFile file) {
        uploadedFiles.add(file);
        file.setWorkspace(this);
    }

    public void addMeeting(Meeting meeting) {
        meetings.add(meeting);
        meeting.setWorkspace(this);
    }

    public int getActiveMembersCount() {
        return (int) members.stream()
                .filter(member -> member.getInvitationStatus() == WorkspaceMember.InvitationStatus.ACCEPTED)
                .count();
    }

    public int getActiveTasksCount() {
        return (int) tasks.stream()
                .filter(task -> task.getStatus() != Task.TaskStatus.COMPLETED && task.getStatus() != Task.TaskStatus.CANCELLED)
                .count();
    }

    public double getProgressPercentage() {
        if (tasks.isEmpty()) return 0.0;

        long completedTasks = tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                .count();

        return (completedTasks * 100.0) / tasks.size();
    }

    // Enums
    public enum PrivacyLevel {
        PRIVATE, PUBLIC, PROTECTED
    }

    @Override
    public String toString() {
        return "Workspace{" +
                "workspaceId=" + workspaceId +
                ", name='" + name + '\'' +
                ", creator=" + (creator != null ? creator.getName() : "null") +
                ", isActive=" + isActive +
                ", membersCount=" + getActiveMembersCount() +
                ", tasksCount=" + tasks.size() +
                '}';
    }
}
