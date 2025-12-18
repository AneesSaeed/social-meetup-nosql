package be.he2b.don5.service;

import be.he2b.don5.dto.LoginRequest;
import be.he2b.don5.dto.RegisterRequest;
import be.he2b.don5.model.User;
import be.he2b.don5.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import be.he2b.don5.service.graph.SocialGraphService;

@AllArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final SearchService searchService;
    private final SocialGraphService socialGraphService;

    public User login(LoginRequest request) {
        return userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User register(RegisterRequest request) {
        // Prevent duplicate email
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
        
        // Créer node Neo4j
        socialGraphService.createUserNode(savedUser.getId(), savedUser.getName());
        
        // Synchroniser avec Elasticsearch
        searchService.syncUserToElasticsearch(savedUser);
        
        return savedUser;
    }
}