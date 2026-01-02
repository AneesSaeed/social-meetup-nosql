package be.he2b.don5.search.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.he2b.don5.meetings.domain.Completion;
import be.he2b.don5.search.application.SearchService;
import be.he2b.don5.search.infrastructure.elasticsearch.document.MeetingSearchDocument;
import be.he2b.don5.search.infrastructure.elasticsearch.document.UserSearchDocument;
import lombok.AllArgsConstructor;

/**
 * REST controller for search endpoints.
 *
 * <p>This controller exposes Elasticsearch-based search for users and meetings.
 */
@RestController
@RequestMapping("/api/search")
@AllArgsConstructor
public class SearchController {

    /**
     * Service that contains the search logic.
     */
    private final SearchService searchService;


    /**
     * Searches users by name or bio (fuzzy search).
     *
     * @param query text typed by the user
     * @return matching users from the search index
     */
    @GetMapping("/users")
    public List<UserSearchDocument> searchUsers(@RequestParam String query) {
        return searchService.searchByNameOrBio(query);
    }


    /**
     * Searches users that contain all given interests (AND condition, fuzzy search).
     *
     * @param interests list of interests to match
     * @return matching users from the search index
     */    
    @GetMapping("/users/by-interests-all")
    public List<UserSearchDocument> searchUsersByInterestsAll(@RequestParam List<String> interests) {
        return searchService.searchByInterestsAll(interests);
    }

    /**
     * Searches meetings by status and by location or interests (fuzzy search).
     *
     * @param status meeting status (converted to {@link Completion})
     * @param query text to match against location/interests
     * @return matching meetings from the search index
     */
    @GetMapping("/meetings/by-status")
    public List<MeetingSearchDocument> searchMeetingsByStatus(
            @RequestParam String status,
            @RequestParam String query) {
        return searchService.searchMeetingsByStatusAndLocationOrInterests(
            Completion.valueOf(status.toUpperCase()), 
            query
        );
    }

    /**
     * Searches meetings where a user is organizer or participant, filtered by status,
     * and matching location or interests (fuzzy search).
     *
     * @param userId user id to filter meetings
     * @param status meeting status (converted to {@link Completion})
     * @param query text to match against location/interests
     * @return matching meetings from the search index
     */
    @GetMapping("/meetings/by-user-status")
    public List<MeetingSearchDocument> searchMeetingsByUserAndStatus(
            @RequestParam String userId,
            @RequestParam String status,
            @RequestParam String query
    ) {
        return searchService.searchMeetingsByUserStatusAndQuery(
                userId,
                Completion.valueOf(status.toUpperCase()),
                query
        );
    }
}
