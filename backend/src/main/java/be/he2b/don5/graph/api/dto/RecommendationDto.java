package be.he2b.don5.graph.api.dto;

public record RecommendationDto(
        String userId,
        String userName,
        Long mutualFriends
) {}
