package be.he2b.don5.integration.events;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Wrapper used to send events through Kafka.
 *
 * <p>Contains:
 * <ul>
 *   <li>the event type (what happened)</li>
 *   <li>the event data (payload as JSON)</li>
 * </ul>
 * 
 *
 * <p>This class has an empty constructor so Jackson can deserialize it.
 */
public class EventEnvelope {
    /**
     * Event type (example: USER_CREATED).
     */
    private EventType eventType;

    /**
     * Event payload as JSON.
     */
    private JsonNode data;

    /**
     * Required for Jackson deserialization.
     */
    public EventEnvelope() {}

    /**
     * Creates a new event envelope.
     *
     * @param eventType type of event
     * @param data event payload as JSON
     */
    public EventEnvelope(EventType eventType, JsonNode data) {
        this.eventType = eventType;
        this.data = data;
    }

    /**
     * @return event type
     */
    public EventType getEventType() { return eventType; }

    /**
     * @return payload as JSON
     */
    public JsonNode getData() { return data; }

    /**
     * @param eventType event type
     */
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    /**
     * @param data payload as JSON
     */
    public void setData(JsonNode data) { this.data = data; }
}