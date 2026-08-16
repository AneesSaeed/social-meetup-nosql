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

/**
 * Kafka consumer that updates Neo4j when meetings are completed.
 *
 * <p>Listens to "meeting-events". Only {@link EventType#MEETING_COMPLETED}
 * is processed here.
 */
@Service
@AllArgsConstructor
@Slf4j
public class MeetingEventConsumer {

    /**
     * Service used to create meeting relations in Neo4j.
     */
    private final SocialGraphService socialGraphService;

    /**
     * JSON mapper used to deserialize Kafka messages.
     */
    private final ObjectMapper objectMapper;

    /**
     * Consumes meeting events and creates Neo4j relations for completed meetings.
     *
     * @param message Kafka message containing an {@link EventEnvelope} as JSON
     */
    @KafkaListener(topics = "meeting-events", groupId = "neo4j-meeting-consumer")
    public void consumeMeetingCompleted(String message) throws Exception {
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
    }
}
