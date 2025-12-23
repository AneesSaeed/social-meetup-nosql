package be.he2b.don5.repository.graph;

import be.he2b.don5.domain.graph.UserNode;
import be.he2b.don5.dto.RecommendationDto;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;

public interface UserNodeRepository extends Neo4jRepository<UserNode, String> {

       @Query("MATCH (a:User {id: $userId1}), (b:User {id: $userId2}) " +
                     "CREATE (a)-[:MET {meetingId: $meetingId, points: $points, date: datetime($date), location: $location, interest: $interest}]->(b)")
       void createMeetingRelation(@Param("userId1") String userId1,
                     @Param("userId2") String userId2,
                     @Param("meetingId") String meetingId,
                     @Param("points") int points,
                     @Param("date") String date,
                     @Param("location") String location,
                     @Param("interest") String interest);

       /**
        * Explanations for coalesce :
        * In case the user has no meetings, the sum would return null.
        * Using coalesce ensures that we return 0 instead of a null that would cause issues and make spring cry.
        * Coalesce(expression, value_if_expressionISnull)
        */
       @Query("MATCH (u:User {id: $userId})-[r:MET]-() RETURN coalesce(sum(r.points), 0) as totalScore")
       Integer getTotalScore(@Param("userId") String userId);

       @Query("MATCH (u:User {id: $userId})-[r:MET]-(other:User) " +
                     "RETURN other.id as userId, other.name as userName, r.points as points, r.date as date, r.location as location, r.interest as interest "
                     +
                     "ORDER BY r.date DESC")
       List<Map<String, Object>> getUserMeetings(@Param("userId") String userId);

       // Fix: mutualFriends count
       @Query("MATCH (me:User {id: $userId})-[:MET]->(friend)-[:MET]->(recommendation:User) " +
                     "WHERE NOT (me)-[:MET]-(recommendation) AND me <> recommendation " +
                     "RETURN recommendation.id as userId, recommendation.name as userName, count(*) as mutualFriends " +
                     "ORDER BY mutualFriends DESC")
       List<RecommendationDto> getRecommendations(@Param("userId") String userId);

       @Query("MATCH (u:User {id: $userId})-[r:MET*1..3]-(connected:User) " +
                     "RETURN DISTINCT connected.id as userId, connected.name as userName, length(r) as distance " +
                     "ORDER BY distance, userName")
       List<Map<String, Object>> getSocialNetwork(@Param("userId") String userId);

       @Query("MATCH (a:User {id: $userId1})-[r:MET]-(b:User {id: $userId2}) " +
                     "RETURN count(r) as meetingCount")
       Integer countMeetingsBetweenUsers(@Param("userId1") String userId1, @Param("userId2") String userId2);
}