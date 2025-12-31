package be.he2b.don5.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCreatedEvent {
    private String meetingId;
    private String title;
    private String eventType;
    private LocalDateTime date;
    private String location;
    private String organizer;
    private List<String> participants;
    private Integer maxParticipants;
    private List<String> interests;
    private LocalDateTime createdAt;
}