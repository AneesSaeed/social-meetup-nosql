package be.he2b.don5.integration.kafka;

import be.he2b.don5.integration.events.EventEnvelope;
import be.he2b.don5.integration.events.EventType;
import be.he2b.don5.integration.events.Topics;
import be.he2b.don5.integration.outbox.OutboxEvent;
import be.he2b.don5.integration.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepo.findByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : pendingEvents) {
            try {
                EventType type = EventType.valueOf(event.getEventType());
                String topic = Topics.topicFor(type);

                JsonNode dataNode = objectMapper.readTree(event.getPayload());
                EventEnvelope envelope = new EventEnvelope(type, dataNode);
                String wrappedPayload = objectMapper.writeValueAsString(envelope);

                kafkaTemplate.send(topic, event.getAggregateId(), wrappedPayload);

                event.setProcessed(true);
                outboxRepo.save(event);

                log.info("Published {} for aggregate {}", type, event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish event {}: {}", event.getId(), e.getMessage(), e);
            }
        }
    }
}
