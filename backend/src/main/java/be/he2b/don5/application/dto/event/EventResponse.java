package be.he2b.don5.application.dto.event;

import java.time.LocalDateTime;
import java.util.List;
import be.he2b.don5.domain.event.Event;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EventResponse {
    String id;
    String title;
    String description;
    String eventType;
    LocalDateTime date;
    String location;
    String organizer;
    List<String> participants;
    Integer maxParticipants;
    List<String> interests;
    String status;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .date(event.getDate())
                .location(event.getLocation())
                .organizer(event.getOrganizer())
                .participants(event.getParticipants())
                .maxParticipants(event.getMaxParticipants())
                .interests(event.getInterests())
                .status(event.getStatus())
                .build();
    }
}

