package be.he2b.don5.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreateEventRequest {
    private String title;
    private String description;
    private String eventType;
    private LocalDateTime date;
    private String location;
    private String organizer; // User ID
    private int maxParticipants;
    private String interest;
}