package be.he2b.don5.service.kafka;

import be.he2b.don5.dto.event.MeetingCompletedEvent;
import be.he2b.don5.service.graph.SocialGraphService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@AllArgsConstructor
@Slf4j
public class MeetingEventConsumer {

    private final SocialGraphService socialGraphService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "meeting-events", groupId = "neo4j-meeting-consumer")
    public void consumeMeetingCompleted(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.get("eventType").asText();

            if ("MeetingCompletedEvent".equals(eventType)) {
                MeetingCompletedEvent event = objectMapper.treeToValue(root.get("data"), MeetingCompletedEvent.class);

                socialGraphService.createMeetingRelations(
                        event.getMeetingId(),
                        event.getParticipants(),
                        event.getPointsPerUser(),
                        event.getDate(),
                        event.getLocation(),
                        event.getInterests());

                log.info("Neo4j: Created meeting relations for {}", event.getMeetingId());
            }
        } catch (Exception e) {
            log.error("Meeting consumer error: {}", e.getMessage(), e);
        }
    }
}