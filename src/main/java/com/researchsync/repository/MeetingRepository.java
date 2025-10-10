package com.researchsync.repository;

import com.researchsync.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByWorkspaceWorkspaceIdOrderByScheduledTimeDesc(Long workspaceId);

    @Query("SELECT m FROM Meeting m WHERE m.workspace.workspaceId = :workspaceId AND m.scheduledTime > :currentTime AND m.status = 'SCHEDULED' ORDER BY m.scheduledTime")
    List<Meeting> findUpcomingMeetingsByWorkspace(@Param("workspaceId") Long workspaceId, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT m FROM Meeting m WHERE m.scheduledTime BETWEEN :startTime AND :endTime AND m.status = 'SCHEDULED' AND m.reminderSent = false")
    List<Meeting> findMeetingsNeedingReminder(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<Meeting> findByCreatedByUserIdOrderByScheduledTimeDesc(Long userId);

    @Query("SELECT COUNT(m) FROM Meeting m WHERE m.workspace.workspaceId = :workspaceId AND m.status = 'SCHEDULED'")
    Long countScheduledMeetingsByWorkspace(@Param("workspaceId") Long workspaceId);
}
