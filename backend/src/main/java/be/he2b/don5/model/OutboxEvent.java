package be.he2b.don5.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    @Id
    private String id;
    private String aggregateId; // id de l'entité (userId, meetingId...)
    private String aggregateType; // l'objet : User, Meeting...
    private String eventType; // created, updated, deleted...
    private String payload; // JSON string représentant les données de l'événement
    private LocalDateTime createdAt; // date de création de l'événement
    private boolean processed; // indique si l'événement a été traité

    public OutboxEvent(String aggregateId, String aggregateType, String eventType, String payload) {
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
        this.processed = false;
    }
}