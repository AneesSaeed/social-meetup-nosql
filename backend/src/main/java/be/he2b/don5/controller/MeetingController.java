package be.he2b.don5.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.he2b.don5.dto.CreateMeetingRequest;
import be.he2b.don5.model.Meeting;
import be.he2b.don5.model.Completion;
import be.he2b.don5.service.MeetingService;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    public Meeting createMeeting(@RequestBody CreateMeetingRequest request) {
        return meetingService.createMeeting(request);
    }

    @GetMapping
    public List<Meeting> getAllMeetings(@RequestParam(required = false) String status) {
        if (status != null) {
            return meetingService.getMeetingsByStatus(Completion.valueOf(status.toUpperCase()));
        }
        return meetingService.getAllMeetings();
    }

    @GetMapping("/{id}")
    public Meeting getMeetingById(@PathVariable String id) {
        return meetingService.getMeetingById(id);
    }

    @GetMapping("/organizer/{userId}")
    public List<Meeting> getMeetingsByOrganizer(@PathVariable String userId) {
        return meetingService.getMeetingsByOrganizer(userId);
    }

    @GetMapping("/user/{userId}")
    public List<Meeting> getMeetingsByUser(@PathVariable String userId) {
        return meetingService.getMeetingsByUser(userId);
    }

    @PostMapping("/{id}/join")
    public Meeting joinMeeting(@PathVariable String id, @RequestParam String userId) {
        return meetingService.joinMeeting(id, userId);
    }

    @PostMapping("/{id}/leave")
    public Meeting leaveMeeting(@PathVariable String id, @RequestParam String userId) {
        return meetingService.leaveMeeting(id, userId);
    }

    @PostMapping("/{id}/complete")
    public Meeting complete(@PathVariable String id) {
        return meetingService.completeMeeting(id);
    }

    @PostMapping("/{id}/cancel")
    public Meeting cancel(@PathVariable String id) {
        return meetingService.cancelMeeting(id);
    }

    @GetMapping("/search")
    public List<Meeting> searchMeetingsByInterest(@RequestParam String interest) {
        return meetingService.searchMeetingsByInterest(interest);
    }
}
