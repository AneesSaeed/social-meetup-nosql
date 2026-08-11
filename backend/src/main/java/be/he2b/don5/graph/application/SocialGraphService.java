package be.he2b.don5.graph.application;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.he2b.don5.graph.api.dto.NetworkDto;
import be.he2b.don5.graph.api.dto.RecommendationDto;
import be.he2b.don5.graph.domain.UserNode;
import be.he2b.don5.graph.infrastructure.neo4j.UserNodeRepository;
import lombok.AllArgsConstructor;

/**
 * Application service for the graph module.
 *
 * Responsibilities:
 * - Create graph nodes/relationships (triggered by Kafka events)
 * - Expose graph read operations used by the REST API
 */
@Service
@AllArgsConstructor
public class SocialGraphService {

    private final UserNodeRepository userNodeRepo;

    /**
     * Creates a User node in Neo4j if it does not exist yet.
     * Called after user creation in MongoDB (event-driven).
     */
    @Transactional("neo4jTransactionManager")
    public void createUserNode(String userId, String userName) {
        if (!userNodeRepo.existsById(userId)) {
            userNodeRepo.save(new UserNode(userId, userName));
        }
    }

    /**
     * Creates MET relationships between all participants after a meeting is completed.
     *
     * For each pair (A, B) this creates two directed relationships:
     * - A -> B with points earned by A
     * - B -> A with points earned by B
     *
     * interests is stored as a single joined string in the relationship ("interest" property).
     */
    @Transactional("neo4jTransactionManager")
    public void createMeetingRelations(
            String meetingId, 
            List<String> participants,
            Map<String, Integer> pointsPerUser, 
            String date,
            String location, 
            List<String> interests
    ) {
        String interestStr = (interests == null || interests.isEmpty())
            ? null
            : String.join(", ", interests);

        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                String user1 = participants.get(i);
                String user2 = participants.get(j);

                int pointsUser1 = pointsPerUser.getOrDefault(user1, 10);
                int pointsUser2 = pointsPerUser.getOrDefault(user2, 10);

                userNodeRepo.createMeetingRelation(user1, user2, meetingId, pointsUser1, date, location, interestStr);
                userNodeRepo.createMeetingRelation(user2, user1, meetingId, pointsUser2, date, location, interestStr);
            }
        }
    }

    /**
     * Returns user recommendations based on mutual friends in the graph.
     */
    public List<RecommendationDto> getRecommendations(String userId) {
        return userNodeRepo.getRecommendations(userId);
    }
    
    /**
     * Returns the extended network (up to 5 hops) excluding already-met users.
     */
    public List<NetworkDto> getSocialNetwork(String userId) {
        return userNodeRepo.getSocialNetwork(userId);
    }
}