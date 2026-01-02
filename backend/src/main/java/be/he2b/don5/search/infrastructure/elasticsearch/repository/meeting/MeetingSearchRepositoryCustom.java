package be.he2b.don5.search.infrastructure.elasticsearch.repository.meeting;

import java.util.List;

import be.he2b.don5.search.infrastructure.elasticsearch.document.MeetingSearchDocument;

/**
 * Custom Elasticsearch queries for meetings.
 *
 * <p>These methods build fuzzy queries to search meetings by text fields.
 */
public interface MeetingSearchRepositoryCustom {
    /**
     * Searches meetings by location or interests (fuzzy).
     *
     * @param query search text
     * @return matching meetings
     */
    List<MeetingSearchDocument> searchByLocationOrInterestsFuzzy(String query);
    
    /**
     * Searches meetings by status and by location or interests (fuzzy).
     *
     * @param status meeting status as text
     * @param query search text
     * @return matching meetings
     */
    List<MeetingSearchDocument> searchByStatusAndLocationOrInterestsFuzzy(String status, String query);
    
    /**
     * Searches meetings where the user is organizer or participant, filtered by status,
     * and matching location or interests (fuzzy).
     *
     * @param userId user id
     * @param status meeting status as text
     * @param query search text
     * @return matching meetings
     */
    List<MeetingSearchDocument> searchByUserIdAndStatusAndLocationOrInterestsFuzzy(
        String userId, String status, String query
    );
}
