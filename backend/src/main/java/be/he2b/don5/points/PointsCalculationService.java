package be.he2b.don5.points;

import be.he2b.don5.graph.infrastructure.neo4j.UserNodeRepository;
import be.he2b.don5.users.domain.User;
import be.he2b.don5.users.infrastructure.mongo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that calculates points earned after a meeting.
 *
 * <p>Points are computed per participant, based on:
 * <ul>
 *   <li>a base amount of points</li>
 *   <li>a bonus for a first meeting between two users</li>
 *   <li>a reduction if two users already met before</li>
 *   <li>a bonus when the meeting interests are outside the user's interests</li>
 * </ul>
 * 
 */
@Service
@AllArgsConstructor
public class PointsCalculationService {
    /**
     * Neo4j repository used to count how many times two users have met.
     */
    private final UserNodeRepository userNodeRepo;
    /**
     * MongoDB repository used to load user profile data.
     */    
    private final UserRepository userRepo;
    
    /**
     * Base points earned for interacting with one other participant.
     */
    private static final int BASE_POINTS = 10;
    
    /**
     * Extra points when two users meet for the first time.
     */
    private static final int FIRST_MEETING_BONUS = 15;
    
    /**
     * Bonus points per meeting interest that is not in the user's interests.
     */
    private static final int OUT_OF_COMFORT_ZONE_BONUS = 5;

    /**
     * Calculates points for each participant of a meeting.
     *
     * <p>For each participant, points are the sum of points gained from every
     * other participant in the meeting.
     *
     * @param participants list of user ids participating in the meeting
     * @param meetingInterests meeting interests/tags
     * @return map of userId -> total points earned for this meeting
     */
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
                int pointsForThisPerson = calculatePointsForPair(
                    userId, 
                    otherId, 
                    meetingInterests, 
                    user.getInterests()
                );
                totalPoints += pointsForThisPerson;
            }

            pointsMap.put(userId, totalPoints);
        }

        return pointsMap;
    }

    /**
     * Calculates points for one user interacting with one other user in a meeting.
     *
     * <p>Rules:
     * <ul>
     *   <li>Start from {@link #BASE_POINTS}.</li>
     *   <li>If it is their first meeting, add {@link #FIRST_MEETING_BONUS}.</li>
     *   <li>If they met before, reduce points (up to 50% max reduction).</li>
     *   <li>Add {@link #OUT_OF_COMFORT_ZONE_BONUS} for each meeting interest not in the user's interests.</li>
     *   <li>Final result is at least 1 point.</li>
     * </ul>
     * 
     *
     * @param userId1 user receiving points
     * @param userId2 the other user in the pair
     * @param meetingInterests meeting interests/tags
     * @param userInterests interests of userId1
     * @return points for this pair (minimum 1)
     */
    private int calculatePointsForPair(String userId1, String userId2, List<String> meetingInterests, List<String> userInterests) {
        int points = BASE_POINTS;

        Integer previousMeetings = userNodeRepo.countMeetingsBetweenUsers(userId1, userId2);
        if (previousMeetings == null || previousMeetings == 0) {
            points += FIRST_MEETING_BONUS;
        } else {
            double reduction = Math.min(0.5, previousMeetings * 0.2);
            points = (int) (points * (1 - reduction));
        }

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