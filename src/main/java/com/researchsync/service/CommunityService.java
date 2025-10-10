package com.researchsync.service;

import com.researchsync.model.HelpRequest;
import com.researchsync.model.User;
import com.researchsync.repository.HelpRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommunityService {

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private EmailService emailService;

    public HelpRequest createHelpRequest(HelpRequest helpRequest, User requester) {
        try {
            helpRequest.setRequester(requester);
            helpRequest.setCreatedDate(LocalDateTime.now());

            HelpRequest savedRequest = helpRequestRepository.save(helpRequest);

            // Notify potential helpers
            notifyPotentialHelpers(savedRequest);

            return savedRequest;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create help request: " + e.getMessage());
        }
    }

    public HelpRequest findById(Long requestId) {
        return helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Help request not found with ID: " + requestId));
    }

    public List<HelpRequest> getOpenHelpRequests() {
        return helpRequestRepository.findByStatusOrderByCreatedDateDesc(HelpRequest.Status.OPEN);
    }

    public List<HelpRequest> getUserHelpRequests(User user) {
        return helpRequestRepository.findByRequesterOrderByCreatedDateDesc(user);
    }

    public boolean canUserHelp(User user, Long requestId) {
        try {
            HelpRequest request = findById(requestId);
            return !request.getRequester().getUserId().equals(user.getUserId()) &&
                    request.getStatus() == HelpRequest.Status.OPEN;
        } catch (Exception e) {
            return false;
        }
    }

    public void offerHelp(Long requestId, User helper) {
        try {
            HelpRequest request = findById(requestId);

            if (request.getRequester().getUserId().equals(helper.getUserId())) {
                throw new RuntimeException("You cannot help with your own request");
            }

            if (request.getStatus() != HelpRequest.Status.OPEN) {
                throw new RuntimeException("This help request is no longer open");
            }

            request.assignHelper(helper);
            helpRequestRepository.save(request);

            // Send notification to requester
            emailService.sendHelpOfferNotification(
                    request.getRequester(),
                    helper,
                    request.getTitle()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to offer help: " + e.getMessage());
        }
    }

    public void resolveHelpRequest(Long requestId, User resolver) {
        try {
            HelpRequest request = findById(requestId);

            if (!request.getRequester().getUserId().equals(resolver.getUserId()) &&
                    (request.getHelper() == null || !request.getHelper().getUserId().equals(resolver.getUserId()))) {
                throw new RuntimeException("You don't have permission to resolve this request");
            }

            request.resolveRequest();
            helpRequestRepository.save(request);

            // Send thank you email to helper
            if (request.getHelper() != null) {
                emailService.sendHelpThankYouNotification(
                        request.getHelper(),
                        request.getRequester(),
                        request.getTitle()
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve help request: " + e.getMessage());
        }
    }

    private void notifyPotentialHelpers(HelpRequest helpRequest) {
        try {
            // This is a simplified notification - in real implementation,
            // you would match helpers based on skills, availability, etc.
            System.out.println("📢 New help request: " + helpRequest.getTitle() +
                    " (Category: " + helpRequest.getCategory() + ")");
        } catch (Exception e) {
            System.err.println("Failed to notify potential helpers: " + e.getMessage());
        }
    }

    public List<HelpRequest> getHelpRequestsByCategory(HelpRequest.Category category) {
        return helpRequestRepository.findByCategoryAndStatusOrderByCreatedDateDesc(category, HelpRequest.Status.OPEN);
    }

    public List<HelpRequest> getUrgentHelpRequests() {
        return helpRequestRepository.findByUrgencyAndStatusOrderByCreatedDateDesc(
                HelpRequest.Urgency.HIGH, HelpRequest.Status.OPEN);
    }
}
