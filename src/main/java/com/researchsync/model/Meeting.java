package com.researchsync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @NotBlank(message = "Meeting title is required")
    @Size(min = 2, max = 200, message = "Meeting title must be between 2 and 200 characters")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 60;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status = MeetingStatus.SCHEDULED;

    @Column(name = "reminder_sent")
    private boolean reminderSent = false;

    // Constructors
    public Meeting() {
        this.createdDate = LocalDateTime.now();
    }

    public Meeting(String title, LocalDateTime scheduledTime, Workspace workspace, User createdBy) {
        this();
        this.title = title;
        this.scheduledTime = scheduledTime;
        this.workspace = workspace;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }

    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public MeetingStatus getStatus() { return status; }
    public void setStatus(MeetingStatus status) { this.status = status; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }

    // Utility Methods
    public boolean isUpcoming() {
        return scheduledTime.isAfter(LocalDateTime.now()) && status == MeetingStatus.SCHEDULED;
    }

    public boolean isToday() {
        return scheduledTime.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    public LocalDateTime getEndTime() {
        return scheduledTime.plusMinutes(durationMinutes != null ? durationMinutes : 60);
    }

    // Enums
    public enum MeetingStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "meetingId=" + meetingId +
                ", title='" + title + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", status=" + status +
                ", workspace=" + (workspace != null ? workspace.getName() : "null") +
                '}';
    }
}
