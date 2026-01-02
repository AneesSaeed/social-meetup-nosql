package be.he2b.don5.meetings.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a meeting stored in MongoDB.
 *
 * <p>A meeting starts as {@link Completion#UPCOMING}. Users can join or leave
 * until it is completed or cancelled.</p>
 */
@Document(collection = "meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    /**
     * Unique identifier of the meeting in MongoDB.
     */
    @Id
    private String id;

    /**
     * Meeting title.
     */
    private String title;

    /**
     * Meeting description.
     */
    private String description;

    /**
     * Meeting type (example: sport, culture, food).
     */
    private String eventType;

    /**
     * Date and time of the meeting.
     */
    private LocalDateTime date;

    /**
     * Location of the meeting.
     */
    private String location;

    /**
     * Organizer user id.
     */
    private String organizer;

    /**
     * List of participant user ids (includes the organizer).
     */
    private List<String> participants;

    /**
     * Maximum number of participants allowed.
     */
    private int maxParticipants;

    /**
     * Interests/tags of the meeting.
     */
    private List<String> interests;

    /**
     * Points stored on the meeting when completed (0 when upcoming).
     */
    private int points;

    /**
     * Current meeting status.
     */
    private Completion status;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Creates a meeting for creation flow.
     *
     * <p>Defaults:
     * <ul>
     *   <li>organizer is added to participants</li>
     *   <li>status is UPCOMING</li>
     *   <li>points is 0</li>
     *   <li>createdAt is now</li>
     * </ul>
     * </p>
     *
     * @param title meeting title
     * @param description meeting description
     * @param eventType meeting type
     * @param date meeting date/time
     * @param location meeting location
     * @param organizer organizer user id
     * @param maxParticipants maximum participants
     * @param interests meeting interests
     */
    public Meeting(String title, String description, String eventType, LocalDateTime date,
                   String location, String organizer, int maxParticipants, List<String> interests) {
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.date = date;
        this.location = location;
        this.organizer = organizer;
        this.maxParticipants = maxParticipants;
        this.interests = interests;

        this.participants = new ArrayList<>();
        this.participants.add(organizer);

        this.status = Completion.UPCOMING;
        this.points = 0;
        this.createdAt = LocalDateTime.now();
    }
}