package be.he2b.don5.application.dto.user;

import java.util.List;
import be.he2b.don5.domain.user.User;
import be.he2b.don5.domain.user.UserStats;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {
    String id;
    String name;
    String email;
    String bio;
    List<String> interests;
    Integer totalPoints;
    UserStats stats;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .interests(user.getInterests())
                .totalPoints(user.getTotalPoints())
                .stats(user.getStats())
                .build();
    }
}

