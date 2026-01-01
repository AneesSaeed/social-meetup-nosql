package be.he2b.don5.integration.events;

import com.fasterxml.jackson.databind.JsonNode;

public class EventEnvelope {
    private EventType eventType;
    private JsonNode data;

    // REQUIRED for Jackson deserialization
    public EventEnvelope() {}

    public EventEnvelope(EventType eventType, JsonNode data) {
        this.eventType = eventType;
        this.data = data;
    }

    public EventType getEventType() { return eventType; }
    public JsonNode getData() { return data; }

    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public void setData(JsonNode data) { this.data = data; }
}
