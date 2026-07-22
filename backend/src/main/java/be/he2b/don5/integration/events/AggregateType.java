package be.he2b.don5.integration.events;

/**
 * Type of aggregate that produced an event.
 *
 * <p>Used to classify events by domain object (example: user, meeting).
 */
public enum AggregateType {
    USER,
    MEETING
}
