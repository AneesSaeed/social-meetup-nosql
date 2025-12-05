package be.he2b.don5.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import be.he2b.don5.application.UserService;
import be.he2b.don5.application.dto.user.CreateUserRequest;
import be.he2b.don5.application.dto.user.UpdateUserRequest;
import be.he2b.don5.application.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> all() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponse byId(@PathVariable String id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody @Validated CreateUserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable String id, @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }
}

