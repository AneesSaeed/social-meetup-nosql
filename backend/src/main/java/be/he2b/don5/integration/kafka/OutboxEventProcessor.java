package be.he2b.don5.integration.kafka;

import be.he2b.don5.integration.events.EventEnvelope;
import be.he2b.don5.integration.events.EventType;
import be.he2b.don5.integration.events.Topics;
import be.he2b.don5.integration.outbox.OutboxEvent;
import be.he2b.don5.integration.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional("mongoTransactionManager")
    public void processEvent(OutboxEvent event) {
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