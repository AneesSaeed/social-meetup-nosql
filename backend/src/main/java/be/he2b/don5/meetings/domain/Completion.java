package be.he2b.don5.meetings.domain;

/**
 * Status of a meeting.
 */
public enum Completion {
    /**
     * Meeting is finished and points were awarded.
     */
    COMPLETED,

    /**
     * Meeting was cancelled and will not happen.
     */
    CANCELLED,

    /**
     * Meeting is planned and can still be joined (if space is available) or left.
     */
    UPCOMING
}