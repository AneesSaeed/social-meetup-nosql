package be.he2b.don5.service;

import java.util.List;

import org.springframework.stereotype.Service;

import be.he2b.don5.dto.CreateEventRequest;
import be.he2b.don5.model.Event;
import be.he2b.don5.model.Completion;
import be.he2b.don5.repository.EventRepository;
import be.he2b.don5.repository.UserRepository;

@Service
public class EventService {

    private final EventRepository eventRepo;
    private final UserRepository userRepo;

    public EventService(EventRepository eventRepo, UserRepository userRepo) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
    }

    public Event createEvent(CreateEventRequest request) {
        if (!userRepo.existsById(request.getOrganizer())) {
            throw new RuntimeException("Organizer not found");
        }

        Event event = new Event(
            request.getTitle(),
            request.getDescription(),
            request.getEventType(),
            request.getDate(),
            request.getLocation(),
            request.getOrganizer(),
            request.getMaxParticipants(),
            request.getInterest()
        );

        return eventRepo.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }

    public Event getEventById(String id) {
        return eventRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public List<Event> getEventsByOrganizer(String userId) {
        return eventRepo.findByOrganizer(userId);
    }

    public List<Event> getEventsByStatus(Completion status) {
        return eventRepo.findByStatus(status);
    }

    public Event joinEvent(String eventId, String userId) {
        Event event = getEventById(eventId);
        
        if (!userRepo.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        if (event.getParticipants().size() >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is full");
        }

        if (event.getParticipants().contains(userId)) {
            throw new RuntimeException("User already joined");
        }

        event.getParticipants().add(userId);
        return eventRepo.save(event);
    }

    public Event leaveEvent(String eventId, String userId) {
        Event event = getEventById(eventId);
        
        if (event.getOrganizer().equals(userId)) {
            throw new RuntimeException("Organizer cannot leave event");
        }

        event.getParticipants().remove(userId);
        return eventRepo.save(event);
    }

    public List<Event> searchEventsByInterest(String interest) {
        return eventRepo.findByInterestContainingIgnoreCase(interest);
    }
}