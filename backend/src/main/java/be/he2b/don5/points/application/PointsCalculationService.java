package be.he2b.don5.points.application;

import be.he2b.don5.graph.infrastructure.neo4j.UserNodeRepository;
import be.he2b.don5.users.domain.User;
import be.he2b.don5.users.infrastructure.mongo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PointsCalculationService {

    private final UserNodeRepository userNodeRepo;
    private final UserRepository userRepo;

    private static final int BASE_POINTS = 10;
    private static final int FIRST_MEETING_BONUS = 15;
    private static final int OUT_OF_COMFORT_ZONE_BONUS = 5;

    public Map<String, Integer> calculatePointsForMeeting(List<String> participants, List<String> meetingInterests) {
        Map<String, Integer> pointsMap = new HashMap<>();

        for (int i = 0; i < participants.size(); i++) {
            String userId = participants.get(i);
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) continue;

            int totalPoints = 0;

            for (int j = 0; j < participants.size(); j++) {
                if (i == j) continue;

                String otherId = participants.get(j);
                int pointsForThisPerson = calculatePointsForPair(userId, otherId, meetingInterests, user.getInterests());
                totalPoints += pointsForThisPerson;
            }

            pointsMap.put(userId, totalPoints);
        }

        return pointsMap;
    }

    private int calculatePointsForPair(String userId1, String userId2, List<String> meetingInterests, List<String> userInterests) {
        int points = BASE_POINTS;

        Integer previousMeetings = userNodeRepo.countMeetingsBetweenUsers(userId1, userId2);
        if (previousMeetings == null || previousMeetings == 0) {
            points += FIRST_MEETING_BONUS;
        } else {
            double reduction = Math.min(0.5, previousMeetings * 0.2);
            points = (int) (points * (1 - reduction));
        }

        // Bonus pour chaque intérêt du meeting hors zone de confort
        if (meetingInterests != null && !meetingInterests.isEmpty()) {
            List<String> userInterestsLower = userInterests.stream()
                    .map(String::toLowerCase)
                    .toList();
            
            long outOfComfortZoneCount = meetingInterests.stream()
                    .map(String::toLowerCase)
                    .filter(interest -> !userInterestsLower.contains(interest))
                    .count();
            
            points += (int) (outOfComfortZoneCount * OUT_OF_COMFORT_ZONE_BONUS);
        }

        return Math.max(1, points);
    }
}