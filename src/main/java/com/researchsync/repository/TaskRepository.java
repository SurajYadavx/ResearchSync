package com.researchsync.repository;

import com.researchsync.model.Task;
import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // ADD THIS METHOD TO EXISTING TaskRepository.java
    List<Task> findByWorkspaceAndAssignedTo(Workspace workspace, User assignedTo);

    List<Task> findByWorkspace(Workspace workspace);
    List<Task> findByAssignedTo(User assignedTo);
    List<Task> findByCreatedBy(User createdBy);
    List<Task> findByStatus(Task.TaskStatus status);
    List<Task> findByWorkspaceAndStatus(Workspace workspace, Task.TaskStatus status);
    List<Task> findByPriority(Task.TaskPriority priority);
    List<Task> findByWorkspaceAndPriority(Workspace workspace, Task.TaskPriority priority);

    Long countByWorkspace(Workspace workspace);
    Long countByWorkspaceAndStatus(Workspace workspace, Task.TaskStatus status);
    Long countByAssignedToAndStatus(User assignedTo, Task.TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findActiveTasksByUser(@Param("user") User user);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user AND t.dueDate BETWEEN :startDate AND :endDate AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findTasksDueSoonForUser(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate")
    List<Task> findTasksByDueDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Task t WHERE t.title LIKE %:searchTerm% OR t.description LIKE %:searchTerm%")
    List<Task> findByTitleOrDescriptionContaining(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Task t WHERE t.workspace = :workspace AND (t.title LIKE %:searchTerm% OR t.description LIKE %:searchTerm%)")
    List<Task> findByWorkspaceAndTitleOrDescriptionContaining(@Param("workspace") Workspace workspace, @Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Task t WHERE t.workspace = :workspace ORDER BY t.createdDate DESC")
    List<Task> findRecentTasksByWorkspace(@Param("workspace") Workspace workspace);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user ORDER BY t.createdDate DESC")
    List<Task> findRecentTasksByUser(@Param("user") User user);

    @Query("SELECT t FROM Task t WHERE t.status = 'COMPLETED' AND t.completedDate BETWEEN :startDate AND :endDate")
    List<Task> findTasksCompletedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.workspace = :workspace AND t.createdDate >= :startDate")
    Long countTasksCreatedAfter(@Param("workspace") Workspace workspace, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :user AND t.status = 'COMPLETED' AND t.completedDate >= :startDate")
    Long countCompletedTasksSince(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
}
