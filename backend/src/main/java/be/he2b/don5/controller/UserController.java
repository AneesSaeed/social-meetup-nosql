package be.he2b.don5.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.he2b.don5.dto.UpdateUserProfileRequest;
import be.he2b.don5.model.User;
import be.he2b.don5.service.UserService;
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

    @GetMapping("/interests")
    public List<String> getAllUniqueInterests() {
        return userService.getAllUniqueInterests();
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<User> updateProfile(@PathVariable String id,
                                              @RequestBody UpdateUserProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(id, req));
    }
}
