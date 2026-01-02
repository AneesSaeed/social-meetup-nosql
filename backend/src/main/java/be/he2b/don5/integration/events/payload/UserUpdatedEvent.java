package be.he2b.don5.integration.events.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent when a user is updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {

    /**
     * Updated user id.
     */
    private String userId;

    /**
     * Updated bio.
     */
    private String bio;

    /**
     * Updated interests.
     */
    private List<String> interests;

    /**
     * Updated total points.
     */
    private int totalPoints;

    /**
     * Updated total meetings count.
     */
    private int totalMeetings;
}
