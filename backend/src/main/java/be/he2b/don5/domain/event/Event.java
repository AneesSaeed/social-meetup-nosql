package be.he2b.don5.domain.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "events")
public class Event {
    @Id
    private String id;
    private String title;
    private String description;
    private String eventType;
    private LocalDateTime date;
    private String location;
    private String organizer;
    private List<String> participants = new ArrayList<>();
    private Integer maxParticipants;
    private List<String> interests = new ArrayList<>();
    private String status = "upcoming";
    private LocalDateTime createdAt = LocalDateTime.now();
}

