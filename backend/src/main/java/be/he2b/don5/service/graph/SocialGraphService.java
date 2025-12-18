package be.he2b.don5.service.graph;

import be.he2b.don5.domain.graph.UserNode;
import be.he2b.don5.repository.graph.UserNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SocialGraphService {

    private final UserNodeRepository userNodeRepo;

    @Transactional
    public void createUserNode(String userId, String userName) {
        if (!userNodeRepo.existsById(userId)) {
            userNodeRepo.save(new UserNode(userId, userName));
        }
    }

    @Transactional
    public void createMeetingRelations(String meetingId, List<String> participants,
            Map<String, Integer> pointsPerUser, String date,
            String location, String interest) {
        // Créer relations bidirectionnelles entre tous les participants
        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                String user1 = participants.get(i);
                String user2 = participants.get(j);

                int pointsUser1 = pointsPerUser.getOrDefault(user1, 10);
                int pointsUser2 = pointsPerUser.getOrDefault(user2, 10);

                // Chaque utilisateur a ses propres points dans la relation
                userNodeRepo.createMeetingRelation(user1, user2, meetingId, pointsUser1, date, location, interest);
                userNodeRepo.createMeetingRelation(user2, user1, meetingId, pointsUser2, date, location, interest);
            }
        }
    }

    public Integer getUserTotalScore(String userId) {
        Integer score = userNodeRepo.getTotalScore(userId);
        return score != null ? score : 0;
    }

    public List<Map<String, Object>> getUserMeetings(String userId) {
        return userNodeRepo.getUserMeetings(userId);
    }

    public List<Map<String, Object>> getRecommendations(String userId) {
        return userNodeRepo.getRecommendations(userId);
    }

    public List<Map<String, Object>> getSocialNetwork(String userId) {
        return userNodeRepo.getSocialNetwork(userId);
    }
}