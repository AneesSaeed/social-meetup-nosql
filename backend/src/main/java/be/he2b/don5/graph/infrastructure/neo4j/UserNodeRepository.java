package be.he2b.don5.graph.infrastructure.neo4j;

import be.he2b.don5.graph.api.dto.NetworkDto;
import be.he2b.don5.graph.api.dto.RecommendationDto;
import be.he2b.don5.graph.domain.UserNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Neo4j repository for the social graph.
 *
 * Infrastructure layer:
 * - Contains Cypher queries (database-specific)
 * - Stores user nodes and MET relationships
 * - Exposes graph queries used by the application/service layer
 */
public interface UserNodeRepository extends Neo4jRepository<UserNode, String> {

       /**
        * Creates a directed MET relationship (a)-[:MET]->(b) for a given meeting.
        *
        * The relationship stores metadata about the interaction:
        * - meetingId: meeting identifier (MongoDB)
        * - points: points earned by the source user for meeting the target user
        * - date/location/interest: context used for history
        *
        * Note: SocialGraphService creates two relationships per pair (A->B and B->A)
        * to store each user's own points.
        */
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
        * Returns "people you may want to meet" based on friends-of-friends.
        *
        * Logic:
        * - me -> friend -> recommendation
        * - recommendation must not already be directly connected to me
        * - result is ranked by mutual friends count
        */
       @Query("MATCH (me:User {id: $userId})-[:MET]->(friend)-[:MET]->(recommendation:User) " +
                     "WHERE NOT (me)-[:MET]-(recommendation) AND me <> recommendation " +
                     "RETURN recommendation.id as userId, recommendation.name as userName, count(DISTINCT friend) as mutualFriends "
                     +
                     "ORDER BY mutualFriends DESC")
       List<RecommendationDto> getRecommendations(@Param("userId") String userId);

       /**
        * Returns users connected within 1..5 hops, excluding:
        * - the user themselves
        * - users already directly connected (already met)
        *
        * distance = shorwtest path length (min hops).
        */
       @Query(
              "MATCH p = (u:User {id: $userId})-[:MET*1..5]-(connected:User) " +
              "WHERE connected.id <> $userId AND NOT (u)-[:MET]-(connected) " +
              "WITH connected, min(length(p)) AS distance " +
              "RETURN connected.id AS userId, connected.name AS userName, distance AS distance " +
              "ORDER BY distance, userName"
       )
       List<NetworkDto> getSocialNetwork(@Param("userId") String userId);

       
       /**
        * Counts how many MET relationships exist from userId1 to userId2.
        *
        * Notes:
        * - This query is directional because it matches only (a)-[:MET]->(b).
        * - In our graph model we create MET edges in both directions for each meeting,
        *   so counting one direction is usually enough for “have they met before?” checks.
        *
        * Used by the points logic to detect repeated meetings between two users.
        */
       @Query("MATCH (a:User {id: $userId1})-[r:MET]->(b:User {id: $userId2}) " +
                     "RETURN count(r) as meetingCount")
       Integer countMeetingsBetweenUsers(@Param("userId1") String userId1, @Param("userId2") String userId2);
}