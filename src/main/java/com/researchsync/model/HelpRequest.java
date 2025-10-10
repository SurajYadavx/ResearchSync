package com.researchsync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "help_requests")
public class HelpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgency urgency = Urgency.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "helper_id")
    private User helper;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "tags")
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level")
    private AccessLevel accessLevel = AccessLevel.READ_only;

    // Constructors
    public HelpRequest() {
        this.createdDate = LocalDateTime.now();
    }

    public HelpRequest(String title, String description, Category category, User requester) {
        this();
        this.title = title;
        this.description = description;
        this.category = category;
        this.requester = requester;
    }

    // Getters and Setters
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Urgency getUrgency() { return urgency; }
    public void setUrgency(Urgency urgency) { this.urgency = urgency; }

    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }

    public User getHelper() { return helper; }
    public void setHelper(User helper) { this.helper = helper; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public AccessLevel getAccessLevel() { return accessLevel; }
    public void setAccessLevel(AccessLevel accessLevel) { this.accessLevel = accessLevel; }

    // Utility Methods
    public boolean isOverdue() {
        return deadline != null && deadline.isBefore(LocalDateTime.now()) && status != Status.RESOLVED;
    }

    public boolean isUrgent() {
        return urgency == Urgency.HIGH || urgency == Urgency.CRITICAL;
    }

    public void assignHelper(User helper) {
        this.helper = helper;
        this.status = Status.IN_PROGRESS;
    }

    public void resolveRequest() {
        this.status = Status.RESOLVED;
        this.resolvedDate = LocalDateTime.now();
    }

    // Enums
    public enum Category {
        TECHNICAL, RESEARCH, DOCUMENTATION, CODING, DATABASE, UI_UX, TESTING, DEPLOYMENT, OTHER
    }

    public enum Urgency {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED, CANCELLED
    }

    public enum AccessLevel {
        READ_only, limited, guided, full_access
    }

    @Override
    public String toString() {
        return "HelpRequest{" +
                "requestId=" + requestId +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", urgency=" + urgency +
                ", status=" + status +
                ", requester=" + (requester != null ? requester.getName() : "null") +
                ", helper=" + (helper != null ? helper.getName() : "null") +
                '}';
    }
}
