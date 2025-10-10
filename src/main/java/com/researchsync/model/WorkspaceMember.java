package com.researchsync.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_members")
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", nullable = false)
    private InvitationStatus invitationStatus = InvitationStatus.PENDING;

    @Column(name = "invited_date")
    private LocalDateTime invitedDate;

    @Column(name = "joined_date")
    private LocalDateTime joinedDate;

    @Column(name = "invited_by_user_id")
    private Long invitedByUserId;

    // Constructors
    public WorkspaceMember() {
        this.invitedDate = LocalDateTime.now();
    }

    public WorkspaceMember(Workspace workspace, User user, Role role) {
        this();
        this.workspace = workspace;
        this.user = user;
        this.role = role;
    }

    // Getters and Setters
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public InvitationStatus getInvitationStatus() { return invitationStatus; }
    public void setInvitationStatus(InvitationStatus invitationStatus) { this.invitationStatus = invitationStatus; }

    public LocalDateTime getInvitedDate() { return invitedDate; }
    public void setInvitedDate(LocalDateTime invitedDate) { this.invitedDate = invitedDate; }

    public LocalDateTime getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDateTime joinedDate) { this.joinedDate = joinedDate; }

    public Long getInvitedByUserId() { return invitedByUserId; }
    public void setInvitedByUserId(Long invitedByUserId) { this.invitedByUserId = invitedByUserId; }

    // Utility methods
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isMember() {
        return role == Role.MEMBER;
    }

    public boolean isAccepted() {
        return invitationStatus == InvitationStatus.ACCEPTED;
    }

    public boolean isPending() {
        return invitationStatus == InvitationStatus.PENDING;
    }

    public boolean isDeclined() {
        return invitationStatus == InvitationStatus.DECLINED;
    }

    // Enums
    public enum Role {
        ADMIN, MEMBER, VIEWER
    }

    public enum InvitationStatus {
        PENDING, ACCEPTED, DECLINED, EXPIRED
    }

    @Override
    public String toString() {
        return "WorkspaceMember{" +
                "memberId=" + memberId +
                ", workspace=" + (workspace != null ? workspace.getName() : "null") +
                ", user=" + (user != null ? user.getName() : "null") +
                ", role=" + role +
                ", invitationStatus=" + invitationStatus +
                ", joinedDate=" + joinedDate +
                '}';
    }
}
