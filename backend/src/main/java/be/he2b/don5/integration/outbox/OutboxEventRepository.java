package be.he2b.don5.integration.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository interface for managing OutboxEvent entities in MongoDB.
 * Provides a method to retrieve unprocessed outbox events ordered by their creation time.
 */
public interface OutboxEventRepository extends MongoRepository<OutboxEvent, String> {
    List<OutboxEvent> findByProcessedFalseOrderByCreatedAtAsc();
}