package be.he2b.don5.graph.api.dto;

/**
 * API DTO representing a recommended user to meet.
 *
 * mutualFriends = number of distinct users that both
 * the current user and the recommended user have met.
 *
 * Used to rank recommendations (higher = more relevant).
 */
public record RecommendationDto(
        String userId,
        String userName,
        Long mutualFriends
) {}
