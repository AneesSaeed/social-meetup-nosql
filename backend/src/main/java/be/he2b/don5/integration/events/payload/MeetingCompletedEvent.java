package be.he2b.don5.integration.events.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Payload sent when a meeting is completed.
 *
 * <p>Contains the participants and the points given to each user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCompletedEvent {
    /**
     * Completed meeting id.
     */
    private String meetingId;

    /**
     * List of participant user ids.
     */
    private List<String> participants;

    /**
     * Points awarded per user (userId -> points).
     */
    private Map<String, Integer> pointsPerUser;

    /**
     * Meeting date as a string.
     */
    private String date;

    /**
     * Meeting location.
     */
    private String location;

    /**
     * Meeting interests/tags.
     */
    private List<String> interests;
}