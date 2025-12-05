package be.he2b.don5.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByStatus(String status);
    List<Event> findByDateAfter(LocalDateTime date);
    List<Event> findByOrganizer(String organizer);
}

