package be.he2b.don5.integration.events;

/**
 * Kafka topics used for integration events.
 *
 * <p>Provides a helper method to choose the correct topic based on {@link EventType}.</p>
 */
public final class Topics {
    private Topics() {}
    
    /**
     * Topic for user events.
     */
    public static final String USER_EVENTS = "user-events";
    /**
     * Topic for meeting events.
     */
    public static final String MEETING_EVENTS = "meeting-events";

    /**
     * Returns the Kafka topic name for a given event type.
     *
     * @param eventType event type
     * @return topic name
     */
    public static String topicFor(EventType eventType) {
        return switch (eventType) {
            case USER_CREATED, USER_UPDATED -> USER_EVENTS;
            case MEETING_CREATED, MEETING_UPDATED, MEETING_COMPLETED, MEETING_CANCELLED -> MEETING_EVENTS;
        };
    }
}
