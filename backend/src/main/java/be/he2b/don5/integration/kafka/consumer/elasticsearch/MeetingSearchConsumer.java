package be.he2b.don5.integration.kafka.consumer.elasticsearch;

import be.he2b.don5.integration.events.EventEnvelope;
import be.he2b.don5.integration.events.payload.MeetingCancelledEvent;
import be.he2b.don5.integration.events.payload.MeetingCompletedEvent;
import be.he2b.don5.integration.events.payload.MeetingCreatedEvent;
import be.he2b.don5.integration.events.payload.MeetingUpdatedEvent;
import be.he2b.don5.search.infrastructure.elasticsearch.document.MeetingSearchDocument;
import be.he2b.don5.search.infrastructure.elasticsearch.repository.meeting.MeetingSearchRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Kafka consumer that updates the Elasticsearch "meetings" index.
 *
 * <p>Listens to "meeting-events" and keeps the {@link MeetingSearchDocument}
 * index in sync with meeting events.</p>
 */
@Service
@AllArgsConstructor
@Slf4j
public class MeetingSearchConsumer {

    /**
     * Elasticsearch repository for meeting documents.
     */
    private final MeetingSearchRepository meetingSearchRepository;

    /**
     * JSON mapper used to deserialize Kafka messages.
     */
    private final ObjectMapper objectMapper;

    /**
     * Receives meeting events and updates Elasticsearch.
     *
     * @param message Kafka message containing an {@link EventEnvelope} as JSON
     */
    @KafkaListener(topics = "meeting-events", groupId = "elasticsearch-meeting-consumer")
    public void consumeForElasticsearch(String message) {
        try {
            EventEnvelope env = objectMapper.readValue(message, EventEnvelope.class);

            switch (env.getEventType()) {
                case MEETING_CREATED -> 
                        handleMeetingCreated(objectMapper.treeToValue(env.getData(), MeetingCreatedEvent.class));
                case MEETING_UPDATED -> 
                        handleMeetingUpdated(objectMapper.treeToValue(env.getData(), MeetingUpdatedEvent.class));
                case MEETING_COMPLETED -> 
                        handleMeetingCompleted(objectMapper.treeToValue(env.getData(), MeetingCompletedEvent.class));
                case MEETING_CANCELLED -> 
                        handleMeetingCancelled(objectMapper.treeToValue(env.getData(), MeetingCancelledEvent.class));
                default -> log.warn("Ignored event type: {}", env.getEventType());
            }
        } catch (Exception e) {
            log.error("Elasticsearch meeting consumer error: {}", e.getMessage(), e);
        }
    }

    /**
     * Indexes a new meeting document when a meeting is created.
     *
     * @param event meeting created payload
     */
    private void handleMeetingCreated(MeetingCreatedEvent event) {
        MeetingSearchDocument doc = new MeetingSearchDocument();
        doc.setMeetingId(event.getMeetingId());
        doc.setTitle(event.getTitle());
        doc.setEventType(event.getEventType());
        doc.setDate(event.getDate() != null ? LocalDateTime.parse(event.getDate()) : null);
        doc.setLocation(event.getLocation());
        doc.setOrganizer(event.getOrganizer());
        doc.setParticipants(event.getParticipants());
        doc.setMaxParticipants(event.getMaxParticipants());
        doc.setInterests(event.getInterests());
        doc.setStatus("upcoming");
        doc.setPoints(0);
        doc.setCreatedAt(event.getCreatedAt() != null ? LocalDateTime.parse(event.getCreatedAt()) : null);

        meetingSearchRepository.save(doc);
        log.info("Elasticsearch: Indexed new meeting {}", event.getMeetingId());
    }
    
    /**
     * Updates participants when users join or leave a meeting.
     *
     * @param event meeting updated payload
     */
    private void handleMeetingUpdated(MeetingUpdatedEvent event) {
        meetingSearchRepository.findById(event.getMeetingId()).ifPresent(doc -> {
            doc.setParticipants(event.getParticipants());
            meetingSearchRepository.save(doc);
            log.info("Elasticsearch: Updated meeting {} participants ({})", event.getMeetingId(), event.getAction());
        });
    }

    /**
     * Marks a meeting as completed in the search index.
     *
     * <p>If the document does not exist yet, it creates a minimal one.</p>
     *
     * @param event meeting completed payload
     */
    private void handleMeetingCompleted(MeetingCompletedEvent event) {
        meetingSearchRepository.findById(event.getMeetingId()).ifPresentOrElse(doc -> {
            doc.setStatus("completed");
            doc.setLocation(event.getLocation());
            doc.setInterests(event.getInterests() != null ? event.getInterests() : doc.getInterests());
            doc.setPoints(doc.getPoints() != null ? doc.getPoints() : 0);
            meetingSearchRepository.save(doc);
            log.info("Elasticsearch: Updated meeting {} to COMPLETED", event.getMeetingId());
        }, () -> {
            MeetingSearchDocument doc = new MeetingSearchDocument();
            doc.setMeetingId(event.getMeetingId());
            doc.setLocation(event.getLocation());
            doc.setInterests(event.getInterests() != null ? event.getInterests() : doc.getInterests());
            doc.setStatus("completed");
            meetingSearchRepository.save(doc);
            log.info("Elasticsearch: Indexed meeting {} as COMPLETED", event.getMeetingId());
        });
    }
    
    /**
     * Marks a meeting as cancelled in the search index.
     *
     * @param event meeting cancelled payload
     */
    private void handleMeetingCancelled(MeetingCancelledEvent event) {
        meetingSearchRepository.findById(event.getMeetingId()).ifPresent(doc -> {
            doc.setStatus("cancelled");
            meetingSearchRepository.save(doc);
            log.info("Elasticsearch: Updated meeting {} to CANCELLED", event.getMeetingId());
        });
    }
}
