package com.researchsync.service;

import com.researchsync.model.Meeting;
import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import com.researchsync.model.WorkspaceMember;
import com.researchsync.repository.MeetingRepository;
import com.researchsync.repository.WorkspaceMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private EmailService emailService;

    public Meeting createMeeting(Meeting meeting, Workspace workspace, User creator) {
        try {
            meeting.setWorkspace(workspace);
            meeting.setCreatedBy(creator);
            meeting.setCreatedDate(LocalDateTime.now());

            Meeting savedMeeting = meetingRepository.save(meeting);

            sendMeetingNotifications(savedMeeting);

            return savedMeeting;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create meeting: " + e.getMessage());
        }
    }

    public List<Meeting> getWorkspaceMeetings(Long workspaceId) {
        return meetingRepository.findByWorkspaceWorkspaceIdOrderByScheduledTimeDesc(workspaceId);
    }

    public List<Meeting> getUpcomingMeetings(Long workspaceId) {
        return meetingRepository.findUpcomingMeetingsByWorkspace(workspaceId, LocalDateTime.now());
    }

    private void sendMeetingNotifications(Meeting meeting) {
        try {
            List<WorkspaceMember> members = workspaceMemberRepository
                    .findByWorkspaceAndInvitationStatus(meeting.getWorkspace(), WorkspaceMember.InvitationStatus.ACCEPTED);

            for (WorkspaceMember member : members) {
                emailService.sendMeetingNotification(
                        member.getUser(),
                        meeting.getTitle(),
                        meeting.getScheduledTime(),
                        meeting.getMeetingLink(),
                        meeting.getWorkspace().getName()
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to send meeting notifications: " + e.getMessage());
        }
    }

    public Meeting findById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found with ID: " + meetingId));
    }

    public void sendMeetingReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderTime = now.plusMinutes(15); // 15 minutes from now

            // FIXED: Pass both required parameters
            List<Meeting> upcomingMeetings = meetingRepository.findMeetingsNeedingReminder(now, reminderTime);

            for (Meeting meeting : upcomingMeetings) {
                if (!meeting.isReminderSent()) {
                    sendMeetingNotifications(meeting);
                    meeting.setReminderSent(true);
                    meetingRepository.save(meeting);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send meeting reminders: " + e.getMessage());
        }
    }
}
