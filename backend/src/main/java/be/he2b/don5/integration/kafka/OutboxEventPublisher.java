package be.he2b.don5.integration.kafka;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.he2b.don5.integration.outbox.OutboxEvent;
import be.he2b.don5.integration.outbox.OutboxEventRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // every 5 seconds
    @Transactional // atomic operation
    public void publishPendingEvents() {
        // Retrieve unprocessed events
        List<OutboxEvent> pendingEvents = outboxRepo.findByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : pendingEvents) {
            try {
                // Determine topic based on event type
                String topic = "user-events";
                if (event.getEventType().contains("Meeting")) {
                    topic = "meeting-events";
                }

                // Wrapper avec le type d'événement
                String wrappedPayload = String.format(
                        "{\"eventType\":\"%s\",\"data\":%s}",
                        event.getEventType(),
                        event.getPayload());

                /**
                 * Example of wrapped payload:
                 *  {
                      "eventType": "UserCreatedEvent",
                      "data": {
                        "userId": "abc123",
                        "name": "John Doe",
                        "email": "john@example.com",
                        ...
                      }
                    }
                 */

                // Publish to Kafka
                kafkaTemplate.send(topic, event.getAggregateId(), wrappedPayload);

                // Mark event as processed
                event.setProcessed(true);
                // Save the updated event status
                outboxRepo.save(event);

                log.info("Published event {} for aggregate {}", event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                // Log the error but continue processing other events
                log.error("Failed to publish event {}: {}", event.getId(), e.getMessage());
                // The event remains unprocessed and will be retried in the next scheduled run
            }
        }
    }
}