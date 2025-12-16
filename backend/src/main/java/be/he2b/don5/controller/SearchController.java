package be.he2b.don5.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.he2b.don5.domain.search.UserSearchDocument;
import be.he2b.don5.service.SearchService;
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
}
