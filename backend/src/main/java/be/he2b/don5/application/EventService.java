package be.he2b.don5.application;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.he2b.don5.application.dto.event.CreateEventRequest;
import be.he2b.don5.application.dto.event.EventResponse;
import be.he2b.don5.application.dto.event.UpdateEventRequest;
import be.he2b.don5.domain.event.Event;
import be.he2b.don5.domain.event.EventRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setDate(request.getDate());
        event.setLocation(request.getLocation());
        event.setOrganizer(request.getOrganizer());
        event.setMaxParticipants(request.getMaxParticipants());
        if (request.getInterests() != null) {
            event.setInterests(request.getInterests());
        }
        Event saved = eventRepository.save(event);
        return EventResponse.from(saved);
    }

    public List<EventResponse> listUpcoming() {
        return eventRepository.findByDateAfter(LocalDateTime.now())
                .stream().map(EventResponse::from).toList();
    }

    public EventResponse get(String id) {
        return eventRepository.findById(id).map(EventResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    @Transactional
    public EventResponse update(String id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventType() != null) event.setEventType(request.getEventType());
        if (request.getDate() != null) event.setDate(request.getDate());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getMaxParticipants() != null) event.setMaxParticipants(request.getMaxParticipants());
        if (request.getInterests() != null) event.setInterests(request.getInterests());
        if (request.getStatus() != null) event.setStatus(request.getStatus());
        Event saved = eventRepository.save(event);
        return EventResponse.from(saved);
    }

    @Transactional
    public void delete(String id) {
        eventRepository.deleteById(id);
    }

    @Transactional
    public EventResponse join(String id, String userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (!event.getParticipants().contains(userId)) {
            event.getParticipants().add(userId);
        }
        Event saved = eventRepository.save(event);
        return EventResponse.from(saved);
    }

    @Transactional
    public EventResponse leave(String id, String userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        event.getParticipants().remove(userId);
        Event saved = eventRepository.save(event);
        return EventResponse.from(saved);
    }
}

