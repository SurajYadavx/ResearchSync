package com.researchsync.repository;

import com.researchsync.model.HelpRequest;
import com.researchsync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {

    List<HelpRequest> findByStatusOrderByCreatedDateDesc(HelpRequest.Status status);
    List<HelpRequest> findByRequesterOrderByCreatedDateDesc(User requester);
    List<HelpRequest> findByHelperOrderByCreatedDateDesc(User helper);
    List<HelpRequest> findByCategoryAndStatusOrderByCreatedDateDesc(HelpRequest.Category category, HelpRequest.Status status);
    List<HelpRequest> findByUrgencyAndStatusOrderByCreatedDateDesc(HelpRequest.Urgency urgency, HelpRequest.Status status);

    @Query("SELECT h FROM HelpRequest h WHERE h.status = 'OPEN' AND h.deadline < :currentTime")
    List<HelpRequest> findOverdueRequests(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(h) FROM HelpRequest h WHERE h.requester = :user AND h.status = 'RESOLVED'")
    Long countResolvedRequestsByUser(@Param("user") User user);

    @Query("SELECT COUNT(h) FROM HelpRequest h WHERE h.helper = :user AND h.status = 'RESOLVED'")
    Long countHelpsByUser(@Param("user") User user);
}
