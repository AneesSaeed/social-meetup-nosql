package be.he2b.don5.service;

import be.he2b.don5.model.OutboxEvent;
import be.he2b.don5.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class OutboxService {
    
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishEvent(String aggregateId, String aggregateType, String eventType, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            OutboxEvent event = new OutboxEvent(aggregateId, aggregateType, eventType, jsonPayload);
            outboxRepo.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event to outbox", e);
        }
    }
}