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

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.allUsers();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<User> updateProfile(@PathVariable String id,
                                              @RequestBody UpdateUserProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(id, req));
    }
}
