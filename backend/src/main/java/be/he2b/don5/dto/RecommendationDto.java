package be.he2b.don5.dto;

public record RecommendationDto(
        String userId,
        String userName,
        Long mutualFriends
) {}
