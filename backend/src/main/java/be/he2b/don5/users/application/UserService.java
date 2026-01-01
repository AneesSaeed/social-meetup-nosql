package be.he2b.don5.users.application;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.he2b.don5.integration.events.AggregateType;
import be.he2b.don5.integration.events.EventType;
import be.he2b.don5.integration.events.payload.UserUpdatedEvent;
import be.he2b.don5.integration.outbox.OutboxWriter;
import be.he2b.don5.users.api.dto.UpdateUserProfileRequest;
import be.he2b.don5.users.domain.User;
import be.he2b.don5.users.infrastructure.mongo.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final OutboxWriter outboxService;
    
    public List<User> allUsers() {
        return userRepo.findAll();
    }

    @Cacheable(cacheNames = "user", key = "#id")
    public Optional<User> getById(String id) {
        return userRepo.findById(id);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "user", key = "#id"),
            @CacheEvict(cacheNames = "search", allEntries = true),
            @CacheEvict(cacheNames = "interests", allEntries = true)
    })
    public User updateProfile(String id, UpdateUserProfileRequest req) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.bio() != null) {
            user.setBio(req.bio());
        }

        if (req.interests() != null) {
            List<String> cleaned = req.interests().stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .toList();
            user.setInterests(cleaned);
        }

        User saved = userRepo.save(user);

        UserUpdatedEvent event = new UserUpdatedEvent(
            saved.getId(),
            saved.getBio(),
            saved.getInterests(),
            saved.getTotalPoints(),
            saved.getTotalMeetings()
        );
        
        outboxService.addEvent(
            saved.getId(),
            AggregateType.USER,
            EventType.USER_UPDATED,
            event
        );

        return saved;
    }
}