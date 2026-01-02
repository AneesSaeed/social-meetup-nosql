package be.he2b.don5.users.application;

import java.util.List;
import java.util.Optional;

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

/**
 * Application service for user-related operations.
 *
 * <p>This service reads and updates users in MongoDB and publishes user update
 * events using the outbox mechanism.</p>
 */
@Service
@AllArgsConstructor
public class UserService {
    /**
     * Repository used to access users in MongoDB.
     */
    private final UserRepository userRepo;
    
    /**
     * Outbox writer used to publish domain events (Kafka).
     */    
    private final OutboxWriter outboxService;
    
    public List<User> allUsers() {
        return userRepo.findAll();
    }

    /**
     * Returns a user by id (cached).
     *
     * @param id user id
     * @return user if found, otherwise empty
     */
    @Cacheable(cacheNames = "user", key = "#id")
    public Optional<User> getById(String id) {
        return userRepo.findById(id);
    }

    /**
     * Updates a user's profile (bio and interests).
     *
     * <p>Cache is cleared to keep user/search/interests data up to date.</p>
     * <p>After saving, a {@link UserUpdatedEvent} is written to the outbox.</p>
     *
     * @param id user id
     * @param req profile update request
     * @return updated user
     * @throws RuntimeException if the user does not exist
     */    
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