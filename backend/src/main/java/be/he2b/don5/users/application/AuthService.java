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

/**
 * Application service for authentication-related actions.
 *
 * <p>This service handles simple login and registration. Registration also
 * publishes a {@link UserCreatedEvent} using the outbox mechanism.
 *
 * <p>Note: login here only checks if the user exists by email; it does not
 * validate passwords.
 */
@AllArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final OutboxWriter outboxService;

    /**
     * Logs in a user by email.
     *
     * <p>This method returns the user if the email exists.
     *
     * @param request login request containing the email
     * @return the existing user
     * @throws RuntimeException if the user does not exist
     */    
    public User login(LoginRequest request) {
        return userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Registers a new user and publishes a creation event.
     *
     * <p>This method is transactional: user creation and outbox event writing
     * happen in the same transaction.
     *
     * @param request registration request
     * @return the created user
     * @throws RuntimeException if the email is already registered
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
        
        UserCreatedEvent event = new UserCreatedEvent(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getBio(),
            savedUser.getInterests(),
            savedUser.getTotalPoints()
        );
        
        outboxService.addEvent(
            savedUser.getId(),
            AggregateType.USER,
            EventType.USER_CREATED,
            event
        );
        
        return savedUser;
    }
}