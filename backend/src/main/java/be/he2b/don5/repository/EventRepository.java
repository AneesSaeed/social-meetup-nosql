package be.he2b.don5.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.he2b.don5.model.Event;
import be.he2b.don5.model.Completion;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByOrganizer(String userId);
    List<Event> findByParticipantsContaining(String userId);
    List<Event> findByStatus(Completion status);
}