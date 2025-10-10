package com.researchsync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200, message = "Task title must be between 2 and 200 characters")
    @Column(nullable = false)
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    // Constructors
    public Task() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public Task(String title, String description, Workspace workspace, User createdBy) {
        this();
        this.title = title;
        this.description = description;
        this.workspace = workspace;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) {
        this.status = status;
        this.updatedDate = LocalDateTime.now();
        if (status == TaskStatus.COMPLETED) {
            this.completedDate = LocalDateTime.now();
        }
    }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    // Utility methods
    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDateTime.now()) && status != TaskStatus.COMPLETED;
    }

    public boolean isDueSoon() {
        if (dueDate == null || status == TaskStatus.COMPLETED) {
            return false;
        }
        return dueDate.isBefore(LocalDateTime.now().plusDays(1));
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // FIXED ENUMS - Changed Priority to TaskPriority
    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public enum TaskPriority {
        LOW, MEDIUM, HIGH, URGENT
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", title='" + title + '\'' +
                ", assignedTo=" + (assignedTo != null ? assignedTo.getName() : "unassigned") +
                ", status=" + status +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", createdDate=" + createdDate +
                '}';
    }
}
