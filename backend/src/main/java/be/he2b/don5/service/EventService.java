package be.he2b.don5.service;

import java.util.List;

import org.springframework.stereotype.Service;

import be.he2b.don5.dto.CreateEventRequest;
import be.he2b.don5.model.Event;
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
        // Vérifier que l'organisateur existe
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

    public List<Event> getEventsByStatus(String status) {
        return eventRepo.findByStatus(status);
    }

    public Event joinEvent(String eventId, String userId) {
        Event event = getEventById(eventId);
        
        // Vérifier que l'utilisateur existe
        if (!userRepo.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        // Vérifier que l'event n'est pas complet
        if (event.getParticipants().size() >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is full");
        }

        // Vérifier que l'utilisateur n'est pas déjà inscrit
        if (event.getParticipants().contains(userId)) {
            throw new RuntimeException("User already joined");
        }

        event.getParticipants().add(userId);
        return eventRepo.save(event);
    }

    public Event leaveEvent(String eventId, String userId) {
        Event event = getEventById(eventId);
        
        // L'organisateur ne peut pas quitter son propre event
        if (event.getOrganizer().equals(userId)) {
            throw new RuntimeException("Organizer cannot leave event");
        }

        event.getParticipants().remove(userId);
        return eventRepo.save(event);
    }
}