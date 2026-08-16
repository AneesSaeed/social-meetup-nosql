package be.he2b.don5.integration.kafka.consumer.neo4j;

import be.he2b.don5.graph.application.SocialGraphService;
import be.he2b.don5.integration.events.EventEnvelope;
import be.he2b.don5.integration.events.EventType;
import be.he2b.don5.integration.events.payload.UserCreatedEvent;
import be.he2b.don5.integration.events.payload.UserUpdatedEvent;
import be.he2b.don5.search.infrastructure.elasticsearch.document.UserSearchDocument;
import be.he2b.don5.search.infrastructure.elasticsearch.repository.user.UserSearchRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Kafka consumers for user events.
 *
 * <p>This class contains two listeners:
 * <ul>
 *   <li>one updates Neo4j (create user nodes)</li>
 *   <li>one updates Elasticsearch (index/update user documents)</li>
 * </ul>
 * 
 */
@Service
@AllArgsConstructor
@Slf4j
public class UserEventConsumer {

    /**
     * Service used to update the social graph in Neo4j.
     */
    private final SocialGraphService socialGraphService;

    /**
     * Elasticsearch repository for user documents.
     */
    private final UserSearchRepository userSearchRepository;

    /**
     * JSON mapper used to deserialize Kafka messages.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates a user node in Neo4j when a user is created.
     *
     * @param message Kafka message containing an {@link EventEnvelope} as JSON
     */
    @KafkaListener(topics = "user-events", groupId = "neo4j-consumer")
    public void consumeForNeo4j(String message) throws Exception{
        EventEnvelope env = objectMapper.readValue(message, EventEnvelope.class);

        if (env.getEventType() == EventType.USER_CREATED) {
            UserCreatedEvent event = objectMapper.treeToValue(env.getData(), UserCreatedEvent.class);
            socialGraphService.createUserNode(event.getUserId(), event.getName());
            log.info("Neo4j: Created user node for {}", event.getUserId());
        }
    }

    /**
     * Creates or updates the user document in Elasticsearch.
     *
     * <p>On USER_CREATED: creates a new {@link UserSearchDocument}.
     * <p>On USER_UPDATED: updates bio/interests/score and refreshes lastActive.
     *
     * @param message Kafka message containing an {@link EventEnvelope} as JSON
     */
    @KafkaListener(topics = "user-events", groupId = "elasticsearch-consumer")
    public void consumeForElasticsearch(String message) throws Exception{
        EventEnvelope env = objectMapper.readValue(message, EventEnvelope.class);

        if (env.getEventType() == EventType.USER_CREATED) {
            UserCreatedEvent event = objectMapper.treeToValue(env.getData(), UserCreatedEvent.class);

            UserSearchDocument doc = new UserSearchDocument();
            doc.setUserId(event.getUserId());
            doc.setName(event.getName());
            doc.setEmail(event.getEmail());
            doc.setBio(event.getBio());
            doc.setInterests(event.getInterests());
            doc.setTotalScore(event.getTotalPoints());
            doc.setLastActive(LocalDateTime.now());

            userSearchRepository.save(doc);
            log.info("Elasticsearch: Indexed user {}", event.getUserId());

        } else if (env.getEventType() == EventType.USER_UPDATED) {
            UserUpdatedEvent event = objectMapper.treeToValue(env.getData(), UserUpdatedEvent.class);

            userSearchRepository.findById(event.getUserId()).ifPresent(doc -> {
                doc.setBio(event.getBio());
                doc.setInterests(event.getInterests());
                doc.setTotalScore(event.getTotalPoints());
                doc.setLastActive(LocalDateTime.now());
                userSearchRepository.save(doc);
                log.info("Elasticsearch: Updated user {}", event.getUserId());
            });
        }
    }
}
