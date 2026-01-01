package be.he2b.don5.users.application;

import be.he2b.don5.integration.events.AggregateType;
import be.he2b.don5.integration.events.EventType;
import be.he2b.don5.integration.events.payload.UserCreatedEvent;
import be.he2b.don5.integration.outbox.OutboxWriter;
import be.he2b.don5.users.api.dto.LoginRequest;
import be.he2b.don5.users.api.dto.RegisterRequest;
import be.he2b.don5.users.domain.User;
import be.he2b.don5.users.infrastructure.mongo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final OutboxWriter outboxService;

    public User login(LoginRequest request) {
        return userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Transactional : Guarantees atomic user creation and event publishing.
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        
        User newUser = new User(
                request.getName(),
                request.getEmail(),
                request.getBio(),
                request.getInterests()
        );
        User savedUser = userRepo.save(newUser);
        
        // Create and publish UserCreatedEvent to the outbox
        UserCreatedEvent event = new UserCreatedEvent(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getBio(),
            savedUser.getInterests(),
            savedUser.getTotalPoints()
        );
        
        // Publish the event to Kafka via the outbox
        outboxService.addEvent(
            savedUser.getId(),
            AggregateType.USER,
            EventType.USER_CREATED,
            event
        );
        
        return savedUser;
    }
}