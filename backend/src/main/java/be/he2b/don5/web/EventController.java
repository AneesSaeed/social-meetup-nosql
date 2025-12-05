package be.he2b.don5.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import be.he2b.don5.application.EventService;
import be.he2b.don5.application.dto.event.CreateEventRequest;
import be.he2b.don5.application.dto.event.EventResponse;
import be.he2b.don5.application.dto.event.UpdateEventRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Validated
public class EventController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@RequestBody @Validated CreateEventRequest request) {
        return eventService.create(request);
    }

    @GetMapping
    public List<EventResponse> list() {
        return eventService.listUpcoming();
    }

    @GetMapping("/{id}")
    public EventResponse byId(@PathVariable String id) {
        return eventService.get(id);
    }

    @PutMapping("/{id}")
    public EventResponse update(@PathVariable String id, @RequestBody UpdateEventRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        eventService.delete(id);
    }

    @PostMapping("/{id}/join")
    public EventResponse join(@PathVariable String id, @RequestParam String userId) {
        return eventService.join(id, userId);
    }

    @PostMapping("/{id}/leave")
    public EventResponse leave(@PathVariable String id, @RequestParam String userId) {
        return eventService.leave(id, userId);
    }
}

