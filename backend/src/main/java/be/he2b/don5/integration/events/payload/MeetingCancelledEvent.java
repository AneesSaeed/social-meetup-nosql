package be.he2b.don5.integration.events.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent when a meeting is cancelled.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCancelledEvent {

    /**
     * Cancelled meeting id.
     */
    private String meetingId;
}