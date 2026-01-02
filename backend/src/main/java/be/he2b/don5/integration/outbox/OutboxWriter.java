package be.he2b.don5.integration.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.he2b.don5.integration.events.AggregateType;
import be.he2b.don5.integration.events.EventType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes domain events to the outbox collection.
 *
 * <p>Services call this class to store an event in MongoDB as part of the same
 * transaction as the business action.</p>
 */
@Service
@AllArgsConstructor
public class OutboxWriter {

    /**
     * Repository used to store outbox events.
     */
    private final OutboxEventRepository outboxRepo;

    /**
     * JSON mapper used to serialize payload objects.
     */
    private final ObjectMapper objectMapper;

    /**
     * Adds a new event to the outbox.
     *
     * @param aggregateId entity id (example: userId, meetingId)
     * @param aggregateType entity type
     * @param eventType event type
     * @param payload event payload object (will be converted to JSON)
     * @throws RuntimeException if JSON conversion fails
     */
    @Transactional
    public void addEvent(String aggregateId, AggregateType aggregateType, EventType eventType, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            OutboxEvent event = new OutboxEvent(
                aggregateId, 
                aggregateType.name(), 
                eventType.name(), 
                jsonPayload
            );

            outboxRepo.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write event to outbox", e);
        }
    }
}