package be.he2b.don5.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class CreateMeetingRequest {
    private List<String> participants; // IDs des users
    private LocalDateTime date;
    private String location;
    private String interest;
    private String description;
    private int points; // Points à attribuer (par défaut suggéré: 10)
}