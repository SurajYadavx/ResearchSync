package com.researchsync.repository;

import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    List<Workspace> findByCreator(User creator);
    List<Workspace> findByCreatorOrderByCreatedDateDesc(User creator);
    List<Workspace> findByIsActiveTrue();
    List<Workspace> findByIsActiveFalse();
    List<Workspace> findByNameContainingIgnoreCase(String name);

    @Query("SELECT w FROM Workspace w LEFT JOIN w.members wm WHERE w.creator = :user OR (wm.user = :user AND wm.invitationStatus = 'ACCEPTED')")
    List<Workspace> findWorkspacesByUserInvolvement(@Param("user") User user);

    @Query("SELECT w FROM Workspace w WHERE w.isActive = true ORDER BY w.createdDate DESC")
    List<Workspace> findActiveWorkspacesOrderByCreatedDate();

    @Query("SELECT COUNT(w) FROM Workspace w WHERE w.creator = :user AND w.isActive = true")
    Long countActiveWorkspacesByCreator(@Param("user") User user);

    @Query("SELECT w FROM Workspace w WHERE w.createdDate BETWEEN :startDate AND :endDate")
    List<Workspace> findWorkspacesCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT w FROM Workspace w WHERE w.description LIKE %:searchTerm% OR w.name LIKE %:searchTerm%")
    List<Workspace> searchWorkspacesByNameOrDescription(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(w) FROM Workspace w WHERE w.isActive = true")
    Long countActiveWorkspaces();

    @Query("SELECT COUNT(w) FROM Workspace w WHERE w.creator = :user")
    Long countWorkspacesByCreator(@Param("user") User user);
}
