package be.he2b.don5.integration.kafka.consumer;

import be.he2b.don5.integration.events.MeetingCancelledEvent;
import be.he2b.don5.integration.events.MeetingCompletedEvent;
import be.he2b.don5.integration.events.MeetingCreatedEvent;
import be.he2b.don5.integration.events.MeetingUpdatedEvent;
import be.he2b.don5.search.infrastructure.elasticsearch.MeetingSearchDocument;
import be.he2b.don5.search.infrastructure.elasticsearch.MeetingSearchRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class MeetingSearchConsumer {

    private final MeetingSearchRepository meetingSearchRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "meeting-events", groupId = "elasticsearch-meeting-consumer")
    public void consumeForElasticsearch(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.get("eventType").asText();

            switch (eventType) {
                case "MeetingCreatedEvent":
                    handleMeetingCreated(objectMapper.treeToValue(root.get("data"), MeetingCreatedEvent.class));
                    break;
                case "MeetingUpdatedEvent":
                    handleMeetingUpdated(objectMapper.treeToValue(root.get("data"), MeetingUpdatedEvent.class));
                    break;
                case "MeetingCompletedEvent":
                    handleMeetingCompleted(objectMapper.treeToValue(root.get("data"), MeetingCompletedEvent.class));
                    break;
                case "MeetingCancelledEvent":
                    handleMeetingCancelled(objectMapper.treeToValue(root.get("data"), MeetingCancelledEvent.class));
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Elasticsearch meeting consumer error: {}", e.getMessage(), e);
        }
    }

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

    private void handleMeetingUpdated(MeetingUpdatedEvent event) {
        meetingSearchRepository.findById(event.getMeetingId()).ifPresent(doc -> {
            doc.setParticipants(event.getParticipants());
            meetingSearchRepository.save(doc);
            log.info("Elasticsearch: Updated meeting {} participants ({})", event.getMeetingId(), event.getAction());
        });
    }

    private void handleMeetingCompleted(MeetingCompletedEvent event) {
        meetingSearchRepository.findById(event.getMeetingId()).ifPresentOrElse(doc -> {
            doc.setStatus("completed");
            doc.setLocation(event.getLocation());
            doc.setInterests(event.getInterests() != null ? java.util.List.of(event.getInterests()) : doc.getInterests());
            doc.setPoints(doc.getPoints() != null ? doc.getPoints() : 0);
            meetingSearchRepository.save(doc);
            log.info("Elasticsearch: Updated meeting {} to COMPLETED", event.getMeetingId());
        }, () -> {
            MeetingSearchDocument doc = new MeetingSearchDocument();
            doc.setMeetingId(event.getMeetingId());
            doc.setLocation(event.getLocation());
            doc.setInterests(event.getInterests() != null ? java.util.List.of(event.getInterests()) : null);
            doc.setStatus("completed");
            meetingSearchRepository.save(doc);
            log.info("Elasticsearch: Indexed meeting {} as COMPLETED", event.getMeetingId());
        });
    }

    private void handleMeetingCancelled(MeetingCancelledEvent event) {
        meetingSearchRepository.findById(event.getMeetingId()).ifPresent(doc -> {
            doc.setStatus("cancelled");
            meetingSearchRepository.save(doc);
            log.info("Elasticsearch: Updated meeting {} to CANCELLED", event.getMeetingId());
        });
    }
}