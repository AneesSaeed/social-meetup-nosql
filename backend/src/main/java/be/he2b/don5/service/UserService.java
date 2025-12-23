package be.he2b.don5.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.he2b.don5.model.User;
import be.he2b.don5.repository.UserRepository;
import lombok.AllArgsConstructor;
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    public List<User> allUsers() {
        return userRepo.findAll();
    }

    public List<String> getAllUniqueInterests() {
        return userRepo.findAll()
                .stream()
                .flatMap(user -> user.getInterests().stream())
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Optional<User> getById(String id) {
        return userRepo.findById(id);
    }
}
