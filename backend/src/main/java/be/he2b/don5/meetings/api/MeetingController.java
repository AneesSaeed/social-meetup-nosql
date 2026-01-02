package be.he2b.don5.meetings.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.he2b.don5.meetings.api.dto.CreateMeetingRequest;
import be.he2b.don5.meetings.application.MeetingService;
import be.he2b.don5.meetings.domain.Completion;
import be.he2b.don5.meetings.domain.Meeting;

/**
 * REST controller for meeting endpoints.
 *
 * <p>Provides routes to create meetings, list meetings, join/leave,
 * and change meeting status (complete/cancel).
 */
@RestController
@RequestMapping("/api/meetings")
public class MeetingController {
    /**
     * Service that contains the meeting business logic.
     */
    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    /**
     * Creates a new meeting.
     *
     * @param request meeting creation request
     * @return created meeting
     */
    @PostMapping
    public Meeting createMeeting(@RequestBody CreateMeetingRequest request) {
        return meetingService.createMeeting(request);
    }

    /**
     * Returns all meetings, optionally filtered by status.
     *
     * @param status optional status filter (UPCOMING/COMPLETED/CANCELLED)
     * @return list of meetings
     */
    @GetMapping
    public List<Meeting> getAllMeetings(@RequestParam(required = false) String status) {
        if (status != null) {
            return meetingService.getMeetingsByStatus(Completion.valueOf(status.toUpperCase()));
        }
        return meetingService.getAllMeetings();
    }

    /**
     * Returns a meeting by id.
     *
     * @param id meeting id
     * @return meeting
     */
    @GetMapping("/{id}")
    public Meeting getMeetingById(@PathVariable String id) {
        return meetingService.getMeetingById(id);
    }

    /**
     * Returns meetings where a user is a participant, optionally filtered by status.
     *
     * @param userId user id
     * @param status optional status filter
     * @return list of meetings for the user
     */
    @GetMapping("/user/{userId}")
    public List<Meeting> getMeetingsByUser(
            @PathVariable String userId,
            @RequestParam(required = false) String status
    ) {
        if (status != null && !status.isBlank()) {
            return meetingService.getMeetingsByUserAndStatus(
                    userId,
                    Completion.valueOf(status.toUpperCase())
            );
        }
        return meetingService.getMeetingsByUser(userId);
    }

    /**
     * Adds a user to a meeting (only for UPCOMING meetings).
     *
     * @param id meeting id
     * @param userId user id to join
     * @return updated meeting
     */
    @PostMapping("/{id}/join")
    public Meeting joinMeeting(@PathVariable String id, @RequestParam String userId) {
        return meetingService.joinMeeting(id, userId);
    }

    /**
     * Removes a user from a meeting (only for UPCOMING meetings).
     * Organizer cannot leave their own meeting.
     *
     * @param id meeting id
     * @param userId user id to leave
     * @return updated meeting
     */
    @PostMapping("/{id}/leave")
    public Meeting leaveMeeting(@PathVariable String id, @RequestParam String userId) {
        return meetingService.leaveMeeting(id, userId);
    }

    /**
     * Marks a meeting as completed and awards points to participants.
     *
     * @param id meeting id
     * @return updated meeting
     */
    @PostMapping("/{id}/complete")
    public Meeting complete(@PathVariable String id) {
        return meetingService.completeMeeting(id);
    }

    /**
     * Cancels a meeting.
     *
     * @param id meeting id
     * @return updated meeting
     */
    @PostMapping("/{id}/cancel")
    public Meeting cancel(@PathVariable String id) {
        return meetingService.cancelMeeting(id);
    }
}
