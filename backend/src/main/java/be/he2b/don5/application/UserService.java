package be.he2b.don5.application;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.he2b.don5.application.dto.user.CreateUserRequest;
import be.he2b.don5.application.dto.user.UpdateUserRequest;
import be.he2b.don5.application.dto.user.UserResponse;
import be.he2b.don5.domain.search.UserSearchDocument;
import be.he2b.don5.domain.search.UserSearchRepository;
import be.he2b.don5.domain.user.User;
import be.he2b.don5.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSearchRepository searchRepository;
    private final StringRedisTemplate redis;

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse findById(String id) {
        return userRepository.findById(id).map(UserResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        user.setInterests(request.getInterests());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        syncToSearch(saved);
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse update(String id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (request.getName() != null) user.setName(request.getName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getInterests() != null) user.setInterests(request.getInterests());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        syncToSearch(saved);
        redis.delete(cacheKey(id));
        return UserResponse.from(saved);
    }

    @Transactional
    public void incrementPoints(String userId, Integer points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTotalPoints(user.getTotalPoints() + points);
        user.getStats().setTotalMeetings(user.getStats().getTotalMeetings() + 1);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        redis.delete(cacheKey(userId));
        syncToSearch(user);
    }

    private void syncToSearch(User user) {
        UserSearchDocument doc = new UserSearchDocument();
        doc.setUserId(user.getId());
        doc.setName(user.getName());
        doc.setBio(user.getBio());
        doc.setInterests(user.getInterests());
        doc.setTotalScore(user.getTotalPoints());
        doc.setLastActive(LocalDateTime.now());
        searchRepository.save(doc);
    }

    private String cacheKey(String userId) {
        return "user:" + userId;
    }
}

