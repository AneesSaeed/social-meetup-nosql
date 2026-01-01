package be.he2b.don5.integration.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class OutboxWriter {
    
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishEvent(String aggregateId, String aggregateType, String eventType, Object payload) {
        try {
            // Convert payload to JSON using the JacksonConfig ObjectMapper
            String jsonPayload = objectMapper.writeValueAsString(payload);

            // Create and save the outbox event
            OutboxEvent event = new OutboxEvent(aggregateId, aggregateType, eventType, jsonPayload);

            // Saved in MongoDB
            outboxRepo.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event to outbox", e);
        }
    }
}