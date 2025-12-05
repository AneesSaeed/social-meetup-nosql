package be.he2b.don5.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import be.he2b.don5.application.SearchService;
import be.he2b.don5.domain.search.UserSearchDocument;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/users")
    public List<UserSearchDocument> search(@RequestParam("q") String q) {
        return searchService.search(q);
    }

    @PostMapping("/sync/user/{id}")
    public void syncUser(@PathVariable("id") String userId) {
        searchService.syncUser(userId);
    }

    @PostMapping("/sync/all")
    public void syncAll() {
        searchService.syncAllUsers();
    }
}

