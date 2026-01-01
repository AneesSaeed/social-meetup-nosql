package be.he2b.don5.integration.events;

public final class Topics {
    private Topics() {}

    public static final String USER_EVENTS = "user-events";
    public static final String MEETING_EVENTS = "meeting-events";

    public static String topicFor(EventType eventType) {
        return switch (eventType) {
            case USER_CREATED, USER_UPDATED -> USER_EVENTS;
            case MEETING_CREATED, MEETING_UPDATED, MEETING_COMPLETED, MEETING_CANCELLED -> MEETING_EVENTS;
        };
    }
}
