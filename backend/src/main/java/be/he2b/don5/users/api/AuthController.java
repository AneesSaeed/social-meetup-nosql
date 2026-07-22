package be.he2b.don5.users.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.he2b.don5.users.api.dto.LoginRequest;
import be.he2b.don5.users.api.dto.RegisterRequest;
import be.he2b.don5.users.application.AuthService;
import be.he2b.don5.users.domain.User;

/**
 * REST controller for authentication endpoints.
 *
 * <p>Exposes API routes to login and register users.
 */
@RestController
@RequestMapping("/api")
public class AuthController {
    /**
     * Service that handles login and registration logic.
     */
    private final AuthService authService;
    
    /**
     * Creates the controller.
     *
     * @param authService authentication service
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Logs in a user using the provided email.
     *
     * @param request login request body
     * @return the existing user
     */    
    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Registers a new user.
     *
     * @param request registration request body
     * @return the created user
     */    
    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
