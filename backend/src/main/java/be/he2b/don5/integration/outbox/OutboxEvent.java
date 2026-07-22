package be.he2b.don5.integration.outbox;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an event stored in the outbox collection.
 *
 * <p>The outbox pattern stores events in MongoDB first, then a separate
 * publisher sends them to Kafka. This makes event publishing more reliable.
 */
@Document(collection = "outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    /**
     * Unique id of the outbox event.
     */
    @Id
    private String id;

    /**
     * Id of the entity that produced the event (example: userId, meetingId).
     */
    private String aggregateId;

    /**
     * Type of the entity that produced the event (example: USER, MEETING).
     */
    private String aggregateType;

    /**
     * Name of the event (example: USER_CREATED).
     */
    private String eventType;

    /**
     * Event payload as a JSON string.
     */
    private String payload;

    /**
     * Time when the outbox event was created.
     */
    private LocalDateTime createdAt;

    /**
     * True when the event was successfully published.
     */
    private boolean processed;

    /**
     * Creates a new outbox event (processed=false, createdAt=now).
     *
     * @param aggregateId entity id
     * @param aggregateType entity type
     * @param eventType event name
     * @param payload JSON payload
     */
    public OutboxEvent(String aggregateId, String aggregateType, String eventType, String payload) {
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
        this.processed = false;
    }
}
