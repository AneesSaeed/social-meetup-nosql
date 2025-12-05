package be.he2b.don5.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import be.he2b.don5.application.MeetingService;
import be.he2b.don5.application.dto.meeting.CreateMeetingRequest;
import be.he2b.don5.application.dto.meeting.MeetingResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@Validated
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse create(@RequestBody @Validated CreateMeetingRequest request) {
        return meetingService.createMeeting(request);
    }

    @GetMapping
    public List<MeetingResponse> all() {
        return meetingService.all();
    }

    @GetMapping("/user/{userId}")
    public List<MeetingResponse> forUser(@PathVariable String userId) {
        return meetingService.forUser(userId);
    }

    @GetMapping("/score/{userId}")
    public Integer score(@PathVariable String userId) {
        return meetingService.calculateTotalScore(userId);
    }
}

