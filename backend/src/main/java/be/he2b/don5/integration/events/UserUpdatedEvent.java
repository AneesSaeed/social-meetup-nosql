package be.he2b.don5.integration.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {
    private String userId;
    private String bio;
    private List<String> interests;
    private int totalPoints;
    private int totalMeetings;
}