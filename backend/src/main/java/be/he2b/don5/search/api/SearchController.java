package be.he2b.don5.search.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.he2b.don5.meetings.domain.Completion;
import be.he2b.don5.search.application.SearchService;
import be.he2b.don5.search.infrastructure.elasticsearch.MeetingSearchDocument;
import be.he2b.don5.search.infrastructure.elasticsearch.UserSearchDocument;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/search")
@AllArgsConstructor
public class SearchController {
    
    private final SearchService searchService;

    @PostMapping("/sync")
    public String syncUsers() {
        searchService.syncAllUsersToElasticsearch();
        return "Users synchronized to Elasticsearch";
    }


    @GetMapping("/users")
    public List<UserSearchDocument> searchUsers(@RequestParam String query) {
        return searchService.searchByNameOrBio(query);
    }

    @GetMapping("/users/by-interests")
    public List<UserSearchDocument> searchUsersByInterests(@RequestParam List<String> interests) {
        return searchService.searchByInterests(interests);
    }

    @GetMapping("/users/by-interests-all")
    public List<UserSearchDocument> searchUsersByInterestsAll(@RequestParam List<String> interests) {
        return searchService.searchByInterestsAll(interests);
    }

    // Meetings

    @PostMapping("/meetings/sync")
    public String syncMeetings() {
        searchService.syncAllMeetingsToElasticsearch();
        return "Meetings synchronized to Elasticsearch";
    }

    @GetMapping("/meetings")
    public List<MeetingSearchDocument> searchMeetings(@RequestParam String query) {
        return searchService.searchMeetingsByLocationOrInterests(query);
    }

    @GetMapping("/meetings/by-status")
    public List<MeetingSearchDocument> searchMeetingsByStatus(
            @RequestParam String status,
            @RequestParam String query) {
        return searchService.searchMeetingsByStatusAndLocationOrInterests(Completion.valueOf(status), query);
    }

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
