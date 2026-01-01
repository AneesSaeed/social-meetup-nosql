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

@Service
@AllArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final SocialGraphService socialGraphService;
    private final UserSearchRepository userSearchRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-events", groupId = "neo4j-consumer")
    public void consumeForNeo4j(String message) {
        try {
            EventEnvelope env = objectMapper.readValue(message, EventEnvelope.class);

            if (env.getEventType() == EventType.USER_CREATED) {
                UserCreatedEvent event = objectMapper.treeToValue(env.getData(), UserCreatedEvent.class);
                socialGraphService.createUserNode(event.getUserId(), event.getName());
                log.info("Neo4j: Created user node for {}", event.getUserId());
            }
        } catch (Exception e) {
            log.error("Neo4j consumer error: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user-events", groupId = "elasticsearch-consumer")
    public void consumeForElasticsearch(String message) {
        try {
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
        } catch (Exception e) {
            log.error("Elasticsearch consumer error: {}", e.getMessage(), e);
        }
    }
}
