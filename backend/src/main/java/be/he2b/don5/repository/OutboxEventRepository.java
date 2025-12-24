package be.he2b.don5.repository;

import be.he2b.don5.model.OutboxEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OutboxEventRepository extends MongoRepository<OutboxEvent, String> {
    List<OutboxEvent> findByProcessedFalseOrderByCreatedAtAsc();
}