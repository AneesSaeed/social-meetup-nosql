package be.he2b.don5.service.kafka;

import be.he2b.don5.model.OutboxEvent;
import be.he2b.don5.repository.OutboxEventRepository;
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

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepo.findByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : pendingEvents) {
            try {
                String topic = "user-events";
                if (event.getEventType().contains("Meeting")) {
                    topic = "meeting-events";
                }

                // Wrapper avec le type d'événement
                String wrappedPayload = String.format(
                        "{\"eventType\":\"%s\",\"data\":%s}",
                        event.getEventType(),
                        event.getPayload());

                kafkaTemplate.send(topic, event.getAggregateId(), wrappedPayload);

                event.setProcessed(true);
                outboxRepo.save(event);

                log.info("Published event {} for aggregate {}", event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}