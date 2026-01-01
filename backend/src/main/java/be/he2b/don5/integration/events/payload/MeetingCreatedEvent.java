package be.he2b.don5.integration.events.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCreatedEvent {
    private String meetingId;
    private String title;
    private String eventType;
    private String date;  // String au lieu de LocalDateTime
    private String location;
    private String organizer;
    private List<String> participants;
    private Integer maxParticipants;
    private List<String> interests;
    private String createdAt;  // String au lieu de LocalDateTime
}