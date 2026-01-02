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

/**
 * Publishes outbox events to Kafka.
 *
 * <p>This service runs on a schedule. It:
 * <ul>
 *   <li>loads pending outbox events from MongoDB</li>
 *   <li>wraps the payload into an {@link EventEnvelope}</li>
 *   <li>sends it to the correct Kafka topic</li>
 *   <li>marks the outbox event as processed</li>
 * </ul>
 * </p>
 */
@Service
@AllArgsConstructor
@Slf4j
public class OutboxEventPublisher {
    /**
     * Repository used to read and update outbox events.
     */
    private final OutboxEventRepository outboxRepo;

    /**
     * Kafka producer used to send messages.
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * JSON mapper used to wrap payloads into an {@link EventEnvelope}.
     */
    private final ObjectMapper objectMapper;

    /**
     * Publishes all unprocessed outbox events to Kafka.
     *
     * <p>Runs every 5 seconds. If publishing fails, the event stays unprocessed
     * and will be retried later.</p>
     */
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
