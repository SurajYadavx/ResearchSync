package com.researchsync.repository;

import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import com.researchsync.model.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    Optional<WorkspaceMember> findByWorkspaceAndUser(Workspace workspace, User user);
    List<WorkspaceMember> findByWorkspace(Workspace workspace);
    List<WorkspaceMember> findByUser(User user);
    List<WorkspaceMember> findByRole(WorkspaceMember.Role role);
    List<WorkspaceMember> findByInvitationStatus(WorkspaceMember.InvitationStatus status);

    List<WorkspaceMember> findByWorkspaceAndRole(Workspace workspace, WorkspaceMember.Role role);
    List<WorkspaceMember> findByWorkspaceAndInvitationStatus(Workspace workspace, WorkspaceMember.InvitationStatus status);
    List<WorkspaceMember> findByUserAndInvitationStatus(User user, WorkspaceMember.InvitationStatus status);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.user = :user AND wm.invitationStatus = 'ACCEPTED'")
    List<WorkspaceMember> findAcceptedMembershipsByUser(@Param("user") User user);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.user = :user AND wm.invitationStatus = 'PENDING'")
    List<WorkspaceMember> findPendingInvitationsByUser(@Param("user") User user);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace = :workspace AND wm.invitationStatus = 'ACCEPTED'")
    List<WorkspaceMember> findAcceptedMembersByWorkspace(@Param("workspace") Workspace workspace);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace = :workspace AND wm.invitationStatus = 'PENDING'")
    List<WorkspaceMember> findPendingInvitationsByWorkspace(@Param("workspace") Workspace workspace);

    @Query("SELECT COUNT(wm) FROM WorkspaceMember wm WHERE wm.workspace = :workspace AND wm.invitationStatus = 'ACCEPTED'")
    Long countAcceptedMembersByWorkspace(@Param("workspace") Workspace workspace);

    @Query("SELECT COUNT(wm) FROM WorkspaceMember wm WHERE wm.workspace = :workspace AND wm.role = :role")
    Long countMembersByWorkspaceAndRole(@Param("workspace") Workspace workspace, @Param("role") WorkspaceMember.Role role);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace = :workspace AND wm.role = 'ADMIN' AND wm.invitationStatus = 'ACCEPTED'")
    List<WorkspaceMember> findAdminsByWorkspace(@Param("workspace") Workspace workspace);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.joinedDate BETWEEN :startDate AND :endDate")
    List<WorkspaceMember> findMembersJoinedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    boolean existsByWorkspaceAndUser(Workspace workspace, User user);
}
