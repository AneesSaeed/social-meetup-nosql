package be.he2b.don5.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import be.he2b.don5.dto.UpdateUserProfileRequest;
import be.he2b.don5.model.User;
import be.he2b.don5.repository.UserRepository;
import lombok.AllArgsConstructor;
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final SearchService searchService;
    
    public List<User> allUsers() {
        return userRepo.findAll();
    }

    @Cacheable(cacheNames = "interests", key = "'all'")
    public List<String> getAllUniqueInterests() {
        return userRepo.findAll()
                .stream()
                .flatMap(user -> user.getInterests().stream())
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "user", key = "#id")
    public Optional<User> getById(String id) {
        return userRepo.findById(id);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "user", key = "#id"),
            @CacheEvict(cacheNames = "search", allEntries = true),
            @CacheEvict(cacheNames = "interests", allEntries = true)
    })
    public User updateProfile(String id, UpdateUserProfileRequest req) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // bio (allow empty string)
        if (req.bio() != null) {
            user.setBio(req.bio());
        }

        // interests (replace list)
        if (req.interests() != null) {
            // normalize: trim + remove blanks + de-dupe (case-insensitive)
            List<String> cleaned = req.interests().stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .toList();
            user.setInterests(cleaned);
        }

        User saved = userRepo.save(user);

        // Sync Elasticsearch so search stays consistent
        searchService.syncUserToElasticsearch(saved);

        return saved;
    }
}
