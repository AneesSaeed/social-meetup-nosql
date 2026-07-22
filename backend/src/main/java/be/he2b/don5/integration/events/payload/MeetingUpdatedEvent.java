package be.he2b.don5.integration.events.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent when a meeting is updated.
 *
 * <p>Currently used for join/leave actions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingUpdatedEvent {

    /**
     * Meeting id.
     */
    private String meetingId;

    /**
     * Updated participant list.
     */
    private List<String> participants;

    /**
     * Update action (example: "JOIN" or "LEAVE").
     */
    private String action;
}
