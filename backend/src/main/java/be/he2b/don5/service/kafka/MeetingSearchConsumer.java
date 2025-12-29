package be.he2b.don5.service.kafka;

import be.he2b.don5.domain.search.MeetingSearchDocument;
import be.he2b.don5.domain.search.MeetingSearchRepository;
import be.he2b.don5.dto.event.MeetingCompletedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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

            if ("MeetingCompletedEvent".equals(eventType)) {
                MeetingCompletedEvent event = objectMapper.treeToValue(root.get("data"), MeetingCompletedEvent.class);

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
        } catch (Exception e) {
            log.error("Elasticsearch meeting consumer error: {}", e.getMessage(), e);
        }
    }
}
