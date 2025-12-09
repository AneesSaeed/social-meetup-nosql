package be.he2b.don5.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.he2b.don5.dto.CreateEventRequest;
import be.he2b.don5.model.Event;
import be.he2b.don5.service.EventService;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public Event createEvent(@RequestBody CreateEventRequest request) {
        return eventService.createEvent(request);
    }

    @GetMapping
    public List<Event> getAllEvents(@RequestParam(required = false) String status) {
        if (status != null) {
            return eventService.getEventsByStatus(status);
        }
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public Event getEventById(@PathVariable String id) {
        return eventService.getEventById(id);
    }

    @GetMapping("/organizer/{userId}")
    public List<Event> getEventsByOrganizer(@PathVariable String userId) {
        return eventService.getEventsByOrganizer(userId);
    }

    @PostMapping("/{id}/join")
    public Event joinEvent(@PathVariable String id, @RequestParam String userId) {
        return eventService.joinEvent(id, userId);
    }

    @PostMapping("/{id}/leave")
    public Event leaveEvent(@PathVariable String id, @RequestParam String userId) {
        return eventService.leaveEvent(id, userId);
    }
}