package be.he2b.don5.integration.kafka.consumer.neo4j;

import be.he2b.don5.graph.application.SocialGraphService;
import be.he2b.don5.integration.events.EventEnvelope;
import be.he2b.don5.integration.events.EventType;
import be.he2b.don5.integration.events.payload.MeetingCompletedEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MeetingEventConsumer {

    private final SocialGraphService socialGraphService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "meeting-events", groupId = "neo4j-meeting-consumer")
    public void consumeMeetingCompleted(String message) {
        try {
            EventEnvelope env = objectMapper.readValue(message, EventEnvelope.class);

            if (env.getEventType() == EventType.MEETING_COMPLETED) {
                MeetingCompletedEvent event = objectMapper.treeToValue(env.getData(), MeetingCompletedEvent.class);

                socialGraphService.createMeetingRelations(
                        event.getMeetingId(),
                        event.getParticipants(),
                        event.getPointsPerUser(),
                        event.getDate(),
                        event.getLocation(),
                        event.getInterests()
                );

                log.info("Neo4j: Created meeting relations for {}", event.getMeetingId());
            }
        } catch (Exception e) {
            log.error("Meeting consumer error: {}", e.getMessage(), e);
        }
    }
}
