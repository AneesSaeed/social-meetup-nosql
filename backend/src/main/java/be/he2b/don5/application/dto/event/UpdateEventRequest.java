package be.he2b.don5.application.dto.event;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class UpdateEventRequest {
    private String title;
    private String description;
    private String eventType;
    private LocalDateTime date;
    private String location;
    private Integer maxParticipants;
    private List<String> interests;
    private String status;
}

