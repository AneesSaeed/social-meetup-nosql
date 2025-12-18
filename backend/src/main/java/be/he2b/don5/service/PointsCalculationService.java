package be.he2b.don5.service;

import be.he2b.don5.model.User;
import be.he2b.don5.repository.UserRepository;
import be.he2b.don5.repository.graph.UserNodeRepository;
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

    /**
     * Calcule les points pour chaque participant d'un meeting
     * 
     * @return Map<userId, points>
     */
    public Map<String, Integer> calculatePointsForMeeting(List<String> participants, String meetingInterest) {
        Map<String, Integer> pointsMap = new HashMap<>();

        for (int i = 0; i < participants.size(); i++) {
            String userId = participants.get(i);
            User user = userRepo.findById(userId).orElse(null);
            if (user == null)
                continue;

            int totalPoints = 0;

            // Points pour chaque autre participant
            for (int j = 0; j < participants.size(); j++) {
                if (i == j)
                    continue;

                String otherId = participants.get(j);
                int pointsForThisPerson = calculatePointsForPair(userId, otherId, meetingInterest, user.getInterests());
                totalPoints += pointsForThisPerson;
            }

            pointsMap.put(userId, totalPoints);
        }

        return pointsMap;
    }

    /**
     * Calcule les points pour une paire d'utilisateurs
     */
    private int calculatePointsForPair(String userId1, String userId2, String meetingInterest,
            List<String> userInterests) {
        int points = BASE_POINTS;

        // 1. Vérifier si première rencontre
        Integer previousMeetings = userNodeRepo.countMeetingsBetweenUsers(userId1, userId2);
        if (previousMeetings == null || previousMeetings == 0) {
            points += FIRST_MEETING_BONUS; // Bonus première rencontre
        } else {
            // Réduction progressive: -20% par rencontre précédente (minimum 50%)
            double reduction = Math.min(0.5, previousMeetings * 0.2);
            points = (int) (points * (1 - reduction));
        }

        // 2. Bonus sortie de zone de confort
        if (meetingInterest != null && !userInterests.contains(meetingInterest.toLowerCase())) {
            points += OUT_OF_COMFORT_ZONE_BONUS;
        }

        return Math.max(1, points); // Minimum 1 point
    }
}