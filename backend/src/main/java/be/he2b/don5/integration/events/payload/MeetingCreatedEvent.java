package be.he2b.don5.integration.events.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent when a meeting is created.
 *
 * <p>Date values are stored as strings for easier transport.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCreatedEvent {

    /**
     * Created meeting id.
     */
    private String meetingId;

    /**
     * Meeting title.
     */
    private String title;

    /**
     * Meeting type (example: sport, culture).
     */
    private String eventType;

    /**
     * Meeting date as a string.
     */
    private String date;

    /**
     * Meeting location.
     */
    private String location;

    /**
     * Organizer user id.
     */
    private String organizer;

    /**
     * Participant user ids.
     */
    private List<String> participants;

    /**
     * Maximum participants allowed.
     */
    private Integer maxParticipants;

    /**
     * Meeting interests/tags.
     */
    private List<String> interests;

    /**
     * Creation time as a string.
     */
    private String createdAt;
}
