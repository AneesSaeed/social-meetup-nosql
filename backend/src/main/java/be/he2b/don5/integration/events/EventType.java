package be.he2b.don5.integration.events;

/**
 * All event names used by the system.
 *
 * <p>These values are used in the outbox and in Kafka messages.
 */
public enum EventType {
    USER_CREATED,
    USER_UPDATED,
    
    MEETING_CREATED,
    MEETING_UPDATED,
    MEETING_COMPLETED,
    MEETING_CANCELLED   
}
