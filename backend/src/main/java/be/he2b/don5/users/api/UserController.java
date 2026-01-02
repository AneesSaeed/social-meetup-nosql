package be.he2b.don5.users.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.he2b.don5.users.api.dto.UpdateUserProfileRequest;
import be.he2b.don5.users.application.UserService;
import be.he2b.don5.users.domain.User;
import lombok.AllArgsConstructor;

/**
 * REST controller for user endpoints.
 *
 * <p>Exposes API routes to list users, get one user, and update a user profile.</p>
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Returns all users.
     *
     * @return list of users
     */
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.allUsers();
    }

    /**
     * Returns one user by id.
     *
     * @param id user id
     * @return 200 with the user if found, otherwise 404
     */    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a user's profile (bio and/or interests).
     *
     * @param id user id
     * @param req update request body
     * @return 200 with the updated user
     */    
    @PatchMapping("/users/{id}")
    public ResponseEntity<User> updateProfile(@PathVariable String id,
                                              @RequestBody UpdateUserProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(id, req));
    }
}
