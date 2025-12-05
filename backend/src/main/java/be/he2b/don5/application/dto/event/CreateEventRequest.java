package be.he2b.don5.application.dto.event;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEventRequest {
    @NotBlank
    private String title;
    private String description;
    @NotBlank
    private String eventType;
    @NotNull
    @Future
    private LocalDateTime date;
    @NotBlank
    private String location;
    @NotBlank
    private String organizer;
    private Integer maxParticipants;
    private List<String> interests;
}

