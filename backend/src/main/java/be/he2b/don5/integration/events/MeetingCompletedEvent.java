package be.he2b.don5.integration.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCompletedEvent {
    private String meetingId;
    private List<String> participants;
    private Map<String, Integer> pointsPerUser;
    private String date;
    private String location;
    private String interests;
}