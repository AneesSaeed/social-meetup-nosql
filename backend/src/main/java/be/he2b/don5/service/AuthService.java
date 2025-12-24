package be.he2b.don5.service;

import be.he2b.don5.dto.LoginRequest;
import be.he2b.don5.dto.RegisterRequest;
import be.he2b.don5.dto.event.UserCreatedEvent;
import be.he2b.don5.model.User;
import be.he2b.don5.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final OutboxService outboxService;

    public User login(LoginRequest request) {
        return userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

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
        
        outboxService.publishEvent(
            savedUser.getId(),
            "User",
            "UserCreatedEvent",
            event
        );
        
        return savedUser;
    }
}