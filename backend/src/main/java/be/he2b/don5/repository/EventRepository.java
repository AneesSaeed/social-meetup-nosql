package be.he2b.don5.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.he2b.don5.model.Event;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByOrganizer(String userId);
    List<Event> findByParticipantsContaining(String userId);
    List<Event> findByStatus(String status);
    List<Event> findByInterestsIn(List<String> interests);
}