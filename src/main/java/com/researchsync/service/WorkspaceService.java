package com.researchsync.service;

import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import com.researchsync.model.WorkspaceMember;
import com.researchsync.repository.WorkspaceRepository;
import com.researchsync.repository.WorkspaceMemberRepository;
import com.researchsync.exception.WorkspaceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WorkspaceService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // CREATE WORKSPACE
    public Workspace createWorkspace(Workspace workspace, User creator) {
        try {
            workspace.setCreator(creator);
            workspace.setCreatedDate(LocalDateTime.now());
            workspace.setLastUpdated(LocalDateTime.now());
            workspace.setActive(true);

            Workspace savedWorkspace = workspaceRepository.save(workspace);

            // Add creator as admin member
            WorkspaceMember creatorMember = new WorkspaceMember();
            creatorMember.setWorkspace(savedWorkspace);
            creatorMember.setUser(creator);
            creatorMember.setRole(WorkspaceMember.Role.ADMIN);
            creatorMember.setInvitationStatus(WorkspaceMember.InvitationStatus.ACCEPTED);
            creatorMember.setJoinedDate(LocalDateTime.now());
            workspaceMemberRepository.save(creatorMember);

            return savedWorkspace;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create workspace: " + e.getMessage());
        }
    }

    // FIND WORKSPACE BY ID
    public Workspace findById(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found with ID: " + workspaceId));
    }

    // GET USER WORKSPACES
    public List<Workspace> getUserWorkspaces(User user) {
        try {
            return workspaceRepository.findWorkspacesByUserInvolvement(user);
        } catch (Exception e) {
            return workspaceRepository.findByCreator(user);
        }
    }

    // GET WORKSPACE MEMBERS
    public List<WorkspaceMember> getWorkspaceMembers(Long workspaceId) {
        Workspace workspace = findById(workspaceId);
        return workspaceMemberRepository.findByWorkspaceAndInvitationStatus(workspace, WorkspaceMember.InvitationStatus.ACCEPTED);
    }

    // CHECK IF USER CAN ACCESS WORKSPACE - MISSING METHOD!
    public boolean canUserAccessWorkspace(User user, Long workspaceId) {
        try {
            Workspace workspace = findById(workspaceId);

            // Check if user is creator
            if (workspace.getCreator().getUserId().equals(user.getUserId())) {
                return true;
            }

            // Check if user is member
            Optional<WorkspaceMember> membership = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user);
            return membership.isPresent() && membership.get().getInvitationStatus() == WorkspaceMember.InvitationStatus.ACCEPTED;

        } catch (Exception e) {
            return false;
        }
    }

    // CHECK IF USER IS ADMIN
    public boolean isUserAdminOfWorkspace(User user, Long workspaceId) {
        try {
            Workspace workspace = findById(workspaceId);
            Optional<WorkspaceMember> member = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user);
            return member.isPresent() && member.get().getRole() == WorkspaceMember.Role.ADMIN;
        } catch (Exception e) {
            return false;
        }
    }

    // CHECK IF USER IS MEMBER
    public boolean isUserMemberOfWorkspace(User user, Long workspaceId) {
        try {
            Workspace workspace = findById(workspaceId);
            return workspaceMemberRepository.findByWorkspaceAndUser(workspace, user).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    // INVITE USER TO WORKSPACE
    public void inviteUserToWorkspace(Long workspaceId, String userEmail, WorkspaceMember.Role role, User inviter) {
        try {
            Workspace workspace = findById(workspaceId);

            // Check if inviter is admin
            if (!isUserAdminOfWorkspace(inviter, workspaceId)) {
                throw new RuntimeException("Only workspace admins can invite members");
            }

            User userToInvite;
            try {
                userToInvite = userService.findByEmail(userEmail);
            } catch (Exception e) {
                throw new RuntimeException("User with email " + userEmail + " not found");
            }

            // Check if user is already a member
            Optional<WorkspaceMember> existingMember = workspaceMemberRepository.findByWorkspaceAndUser(workspace, userToInvite);
            if (existingMember.isPresent()) {
                throw new RuntimeException("User is already a member of this workspace");
            }

            // Create invitation
            WorkspaceMember invitation = new WorkspaceMember();
            invitation.setWorkspace(workspace);
            invitation.setUser(userToInvite);
            invitation.setRole(role);
            invitation.setInvitationStatus(WorkspaceMember.InvitationStatus.ACCEPTED); // Auto-accept for now
            invitation.setJoinedDate(LocalDateTime.now());
            invitation.setInvitedByUserId(inviter.getUserId());
            workspaceMemberRepository.save(invitation);

            // Send invitation email
            emailService.sendWorkspaceInvitation(userToInvite, workspace.getName(), inviter.getName());

        } catch (Exception e) {
            throw new RuntimeException("Failed to invite user: " + e.getMessage());
        }
    }

    // UPDATE WORKSPACE
    public Workspace updateWorkspace(Workspace workspace) {
        workspace.setLastUpdated(LocalDateTime.now());
        return workspaceRepository.save(workspace);
    }

    // DELETE WORKSPACE
    public void deleteWorkspace(Long workspaceId, User user) {
        Workspace workspace = findById(workspaceId);

        if (!workspace.getCreator().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Only workspace creator can delete the workspace");
        }

        workspace.setActive(false);
        workspace.setLastUpdated(LocalDateTime.now());
        workspaceRepository.save(workspace);
    }

    // GET CREATED WORKSPACES
    public List<Workspace> getCreatedWorkspaces(User user) {
        return workspaceRepository.findByCreatorOrderByCreatedDateDesc(user);
    }

    // SEARCH WORKSPACES
    public List<Workspace> searchWorkspaces(String searchTerm) {
        return workspaceRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    // COUNT USER WORKSPACES
    public Long countUserWorkspaces(User user) {
        return (long) getUserWorkspaces(user).size();
    }
}
