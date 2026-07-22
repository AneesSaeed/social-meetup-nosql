package be.he2b.don5.meetings.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * Request body used to create a new meeting.
 *
 * <p>Contains the event information (title, date, location, etc.) and the initial
 * meeting settings (max participants, interests, initial participants).
 */
@Data
public class CreateMeetingRequest {
    private String title;
    private String description;
    private String eventType;
    private LocalDateTime date;
    private String location;
    private String organizer;
    private int maxParticipants;
    private List<String> interests;
    private List<String> participants;
}
