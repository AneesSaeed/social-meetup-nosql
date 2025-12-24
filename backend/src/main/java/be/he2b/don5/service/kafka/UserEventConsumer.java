// UserEventConsumer.java
package be.he2b.don5.service.kafka;

import be.he2b.don5.dto.event.UserCreatedEvent;
import be.he2b.don5.dto.event.UserUpdatedEvent;
import be.he2b.don5.domain.search.UserSearchDocument;
import be.he2b.don5.domain.search.UserSearchRepository;
import be.he2b.don5.service.graph.SocialGraphService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.JsonNode;

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
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.get("eventType").asText();

            if ("UserCreatedEvent".equals(eventType)) {
                UserCreatedEvent event = objectMapper.treeToValue(root.get("data"), UserCreatedEvent.class);
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
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.get("eventType").asText();

            if ("UserCreatedEvent".equals(eventType)) {
                UserCreatedEvent event = objectMapper.treeToValue(root.get("data"), UserCreatedEvent.class);

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

            } else if ("UserUpdatedEvent".equals(eventType)) {
                UserUpdatedEvent event = objectMapper.treeToValue(root.get("data"), UserUpdatedEvent.class);

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