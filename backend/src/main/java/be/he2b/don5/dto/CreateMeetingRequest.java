package be.he2b.don5.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreateMeetingRequest {
    private String title;
    private String description;
    private String eventType;
    private LocalDateTime date;
    private String location;
    private String organizer;      // user id
    private int maxParticipants;
    private String interest;
}
