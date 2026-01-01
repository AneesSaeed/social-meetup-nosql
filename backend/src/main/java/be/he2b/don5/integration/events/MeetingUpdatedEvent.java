package be.he2b.don5.integration.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingUpdatedEvent {
    private String meetingId;
    private List<String> participants;
    private String action; // "JOIN" or "LEAVE"
}