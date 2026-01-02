package be.he2b.don5.integration.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * MongoDB repository for {@link OutboxEvent}.
 *
 * <p>Provides a query to fetch all pending events (processed=false) in creation order.</p>
 */
public interface OutboxEventRepository extends MongoRepository<OutboxEvent, String> {
    /**
     * Returns all not-yet-processed outbox events ordered by creation time.
     *
     * @return pending outbox events
     */
    List<OutboxEvent> findByProcessedFalseOrderByCreatedAtAsc();
}