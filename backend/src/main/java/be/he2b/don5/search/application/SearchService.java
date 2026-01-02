package be.he2b.don5.search.application;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import be.he2b.don5.meetings.domain.Completion;
import be.he2b.don5.search.infrastructure.elasticsearch.document.MeetingSearchDocument;
import be.he2b.don5.search.infrastructure.elasticsearch.document.UserSearchDocument;
import be.he2b.don5.search.infrastructure.elasticsearch.repository.meeting.MeetingSearchRepository;
import be.he2b.don5.search.infrastructure.elasticsearch.repository.user.UserSearchRepository;
import lombok.AllArgsConstructor;

/**
 * Application service for search operations.
 *
 * <p>Uses Elasticsearch repositories to search users and meetings.
 * Some results are cached to reduce repeated queries.
 */
@Service
@AllArgsConstructor
public class SearchService {
    
    /**
     * Elasticsearch repository for user search.
     */
    private final UserSearchRepository userSearchRepository;

    /**
     * Elasticsearch repository for meeting search.
     */
    private final MeetingSearchRepository meetingSearchRepository;

    /**
     * Searches users by name or bio (fuzzy).
     *
     * @param query search text
     * @return matching users
     */
    @Cacheable(cacheNames = "search", key = "'nameBio:' + #query")
    public List<UserSearchDocument> searchByNameOrBio(String query) {
        return userSearchRepository.searchByNameOrBioFuzzy(query);
    }

    /**
     * Searches users that contain all given interests (AND, fuzzy).
     *
     * @param interests interests that must all match
     * @return matching users
     */
    @Cacheable(cacheNames = "search", key = "'interestsAll:' + #interests")
    public List<UserSearchDocument> searchByInterestsAll(List<String> interests) {
        return userSearchRepository.searchByInterestsAllFuzzy(interests);
    }

    /**
     * Searches meetings by status and by location or interests (fuzzy).
     *
     * @param status meeting status
     * @param query search text
     * @return matching meetings
     */
    @Cacheable(cacheNames = "search", key = "'meetingsByStatusFuzzy:' + #status + ':' + #query")
    public List<MeetingSearchDocument> searchMeetingsByStatusAndLocationOrInterests(Completion status, String query) {
        return meetingSearchRepository.searchByStatusAndLocationOrInterestsFuzzy(status.name(), query);
    }

    /**
     * Searches meetings for a specific user (organizer or participant), filtered by status,
     * and matching location or interests (fuzzy).
     *
     * @param userId user id
     * @param status meeting status
     * @param query search text
     * @return matching meetings
     */
    public List<MeetingSearchDocument> searchMeetingsByUserStatusAndQuery(String userId, Completion status, String query) {
        return meetingSearchRepository.searchByUserIdAndStatusAndLocationOrInterestsFuzzy(
            userId, 
            status.name(), 
            query
        );
    }
}
