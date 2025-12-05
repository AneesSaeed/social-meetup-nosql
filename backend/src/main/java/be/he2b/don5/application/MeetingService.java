package be.he2b.don5.application;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.he2b.don5.application.dto.meeting.CreateMeetingRequest;
import be.he2b.don5.application.dto.meeting.MeetingResponse;
import be.he2b.don5.domain.meeting.Meeting;
import be.he2b.don5.domain.meeting.MeetingRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final Neo4jClient neo4jClient;
    private final UserService userService;
    private final StringRedisTemplate redis;

    @Transactional
    public MeetingResponse createMeeting(CreateMeetingRequest request) {
        if (request.getParticipants().size() < 2) {
            throw new IllegalArgumentException("Deux participants minimum");
        }
        String user1 = request.getParticipants().get(0);
        String user2 = request.getParticipants().get(1);

        createRelation(user1, user2, request);

        Meeting meeting = new Meeting();
        meeting.setParticipants(request.getParticipants());
        meeting.setDate(LocalDateTime.now());
        meeting.setLocation(request.getLocation());
        meeting.setInterest(request.getInterest());
        meeting.setPoints(request.getPoints());
        meeting.setDescription(request.getDescription());
        Meeting saved = meetingRepository.save(meeting);

        userService.incrementPoints(user1, request.getPoints());
        userService.incrementPoints(user2, request.getPoints());

        redis.delete(scoreKey(user1));
        redis.delete(scoreKey(user2));

        return MeetingResponse.from(saved);
    }

    public List<MeetingResponse> all() {
        return meetingRepository.findAll().stream().map(MeetingResponse::from).toList();
    }

    public List<MeetingResponse> forUser(String userId) {
        return meetingRepository.findByParticipantsContaining(userId)
                .stream()
                .map(MeetingResponse::from)
                .toList();
    }

    public Integer calculateTotalScore(String userId) {
        String cached = redis.opsForValue().get(scoreKey(userId));
        if (cached != null) {
            return Integer.parseInt(cached);
        }

        String cypher = """
                MATCH (u:User {id: $userId})-[r:MET]-()
                RETURN sum(r.points) as totalScore
                """;
        Integer score = neo4jClient.query(cypher)
                .bind(userId).to("userId")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("totalScore").isNull() ? 0 : record.get("totalScore").asInt())
                .one()
                .orElse(0);

        redis.opsForValue().set(scoreKey(userId), score.toString(), Duration.ofMinutes(10));
        return score;
    }

    private void createRelation(String user1, String user2, CreateMeetingRequest request) {
        String cypher = """
                MATCH (a:User {id: $user1}), (b:User {id: $user2})
                MERGE (a)-[r:MET {meetingId: randomUUID()}]->(b)
                SET r.points = $points,
                    r.date = datetime(),
                    r.location = $location,
                    r.interest = $interest
                """;
        neo4jClient.query(cypher)
                .bind(user1).to("user1")
                .bind(user2).to("user2")
                .bind(request.getPoints()).to("points")
                .bind(request.getLocation()).to("location")
                .bind(request.getInterest()).to("interest")
                .run();
    }

    private String scoreKey(String userId) {
        return "score:" + userId;
    }
}

